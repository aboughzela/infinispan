package org.infinispan.loaders.remote.wrapper;

import java.io.IOException;
import java.util.Arrays;

import org.infinispan.io.ByteBuffer;
import org.infinispan.marshall.BufferSizePredictor;
import org.infinispan.marshall.Marshaller;

/**
 * HotRodEntryMarshaller.
 *
 * @author Tristan Tarrant
 * @since 5.2
 */
public class HotRodEntryMarshaller implements Marshaller {

   BufferSizePredictor predictor = new IdentityBufferSizePredictor();

   @Override
   public byte[] objectToByteBuffer(Object obj, int estimatedSize) throws IOException, InterruptedException {
      return (byte[]) obj;
   }

   @Override
   public byte[] objectToByteBuffer(Object obj) throws IOException, InterruptedException {
      return (byte[]) obj;
   }

   @Override
   public Object objectFromByteBuffer(byte[] buf) throws IOException, ClassNotFoundException {
      return buf;
   }

   @Override
   public Object objectFromByteBuffer(byte[] buf, int offset, int length) throws IOException, ClassNotFoundException {
      return Arrays.copyOfRange(buf, offset, offset+length);
   }

   @Override
   public ByteBuffer objectToBuffer(Object o) throws IOException, InterruptedException {
      byte[] b = (byte[])o;
      return new ByteBuffer(b, 0, b.length);
   }

   @Override
   public boolean isMarshallable(Object o) throws Exception {
      return o instanceof byte[];
   }

   @Override
   public BufferSizePredictor getBufferSizePredictor(Object o) {
      return predictor;
   }

   class IdentityBufferSizePredictor implements BufferSizePredictor {

      @Override
      public int nextSize(Object obj) {
         return ((byte[])obj).length;
      }

      @Override
      public void recordSize(int previousSize) {
         // NOOP
      }

   }
}
