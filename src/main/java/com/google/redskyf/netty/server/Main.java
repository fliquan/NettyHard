/**
 * 
 */
package com.google.redskyf.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author flq
 *
 */
public class Main {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, InterruptedException {
		// TODO Auto-generated method stub
		bind(Integer.parseInt(args[0]));
	}

	private static void bind(int port) throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try{
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(workerGroup, workerGroup).channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100).childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							// TODO Auto-generated method stub
						    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(64, Delimiters.lineDelimiter()));
							ch.pipeline().addLast(new StringDecoder());
							ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
								
								@Override
								public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
									// TODO Auto-generated method stub
									String msgStr = (String)msg;
									System.out.println("Recived String: " + msgStr);
									String outputStr = "Recived String: " + msgStr + "\n";
									ByteBuf buf = Unpooled.buffer(outputStr.getBytes().length);
									buf.writeBytes(outputStr.getBytes());
									
									ctx.write(buf);
								}
								
								/* (non-Javadoc)
								 * <p>Title: channelReadComplete</p> 
								 * <p>Description: </p> 
								 * @param ctx
								 * @throws Exception 
								 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelReadComplete(io.netty.channel.ChannelHandlerContext) 
								 */
								@Override
								public void channelReadComplete(ChannelHandlerContext ctx)
								        throws Exception {
								    ctx.flush();
								}
								
								/* (non-Javadoc)
								 * <p>Title: exceptionCaught</p> 
								 * <p>Description: </p> 
								 * @param ctx
								 * @param cause
								 * @throws Exception 
								 * @see io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(io.netty.channel.ChannelHandlerContext, java.lang.Throwable) 
								 */
								@Override
								public void exceptionCaught(ChannelHandlerContext ctx,
								        Throwable cause) throws Exception {
								    ctx.close();
								}
								
							});
						}

					});
			ChannelFuture f = bootstrap.bind(port).sync();
			f.channel().closeFuture().sync();
		}finally{
		    System.out.println("shutdown................................");
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
		
	}

}
