package org.infinispan.commands.read;

import org.infinispan.commands.VisitableCommand;
import org.infinispan.commands.Visitor;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.InvocationContext;

/**
 * Command to calculate the size of the cache
 *
 * @author Manik Surtani (<a href="mailto:manik@jboss.org">manik@jboss.org</a>)
 * @author Mircea.Markus@jboss.com
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @since 4.0
 */
public class SizeCommand extends AbstractLocalCommand implements VisitableCommand {
   private final DataContainer container;

   public SizeCommand(DataContainer container) {
      this.container = container;
   }

   @Override
   public Object acceptVisitor(InvocationContext ctx, Visitor visitor) throws Throwable {
      return visitor.visitSizeCommand(ctx, this);
   }

   @Override
   public Integer perform(InvocationContext ctx) throws Throwable {
      if (noTxModifications(ctx)) {
         return container.size();
      }

      int size = container.size();
      for (CacheEntry e: ctx.getLookedUpEntries().values()) {
         if (e.isCreated()) {
            size ++;
         } else  if (e.isRemoved()) {
            size --;
         }
      }

      return Math.max(size, 0);
   }

   @Override
   public String toString() {
      return "SizeCommand{" +
            "containerSize=" + container.size() +
            '}';
   }
}
