/**   
 *
 * @Title: Main.java 
 * @Package com.google.redskyf.netty.client 
 * @Description: 
 * @author redskyf redskyf@google.com   
 * @date 2017年4月18日 下午3:41:22 
 * @version V1.0.0 
 *  
 */
package com.google.redskyf.netty.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;

/** 
 * @ClassName: Main 
 * @Description: 
 * @author redskyf redskyf@google.com
 * @date 2017-04-18 15:41:22 
 *  
 */
public class Main {

    /** 
     * @Title: main 
     * @Description: 
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO Auto-generated method stub
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            final BlockingQueue<String> dataQueue = new ArrayBlockingQueue<String>(800);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    // TODO Auto-generated method stub
                    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(64, Delimiters.lineDelimiter()));
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        private long count = 0;
                        
                        /* (non-Javadoc)
                         * <p>Title: channelActive</p> 
                         * <p>Description: </p> 
                         * @param ctx
                         * @throws Exception 
                         * @see io.netty.channel.ChannelInboundHandlerAdapter#channelActive(io.netty.channel.ChannelHandlerContext) 
                         */
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // TODO Auto-generated method stub
                            DataProcess dp = new DataProcess(ctx, dataQueue);
                            Thread thread = new Thread(dp);
                            thread.start();
                               
                        }
                        
                        /* (non-Javadoc)
                         * <p>Title: channelRead</p> 
                         * <p>Description: </p> 
                         * @param ctx
                         * @param msg
                         * @throws Exception 
                         * @see io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel.ChannelHandlerContext, java.lang.Object) 
                         */
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg)
                                throws Exception {
                            System.out.println("recive data............");
                            System.out.println(msg);
                        }
                    });
                }
                
            });
            
            ChannelFuture f = bootstrap.connect("localhost", 9999).sync();
            f.channel().closeFuture().sync();
        }finally{
            System.out.println("shutdown .........");
            group.shutdownGracefully();
        }
    }
    
    public static class DataProcess implements Runnable{
        private ChannelHandlerContext ctx;
        
        private BlockingQueue<String> dataQueue = null;
        
        public DataProcess(ChannelHandlerContext ctx, BlockingQueue<String> dataQueue){
            this.ctx = ctx;
            this.dataQueue = dataQueue;
        }
        
        /* (non-Javadoc)
         * <p>Title: run</p> 
         * <p>Description: </p>  
         * @see java.lang.Runnable#run() 
         */
        @Override
        public void run() {
            while(true){
                try {
                    String data = dataQueue.take();
                    
                    

                    System.out.println("==========================================");
                    System.out.println(data);
                    String sendData = data + "\n";
                    byte[] req = sendData.getBytes();
                    ByteBuf msgBuf = Unpooled.buffer(req.length);
                    msgBuf.writeBytes(req);
                    ctx.writeAndFlush(msgBuf);
                    
                    
                    
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
            }
            
        }
    }

}
