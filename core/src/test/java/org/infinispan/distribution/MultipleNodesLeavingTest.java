package org.infinispan.distribution;

import org.infinispan.config.Configuration;
import org.infinispan.remoting.transport.Address;
import org.infinispan.test.MultipleCacheManagersTest;
import org.infinispan.test.TestingUtil;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Mircea Markus
 * @since 5.0
 */
@Test (groups = "functional", testName = "distribution.MultipleNodesLeavingTest")
public class MultipleNodesLeavingTest extends MultipleCacheManagersTest {

   @Override
   protected void createCacheManagers() throws Throwable {
      createCluster(getDefaultClusteredConfig(Configuration.CacheMode.DIST_SYNC), 4);
      waitForClusterToForm();
   }

   public void testMultipleLeaves() throws Exception {

      //kill 3 caches at once
      fork(new Runnable() {
         @Override
         public void run() {
            manager(3).stop();
         }
      }, false);

      fork(new Runnable() {
         @Override
         public void run() {
            manager(2).stop();
         }
      }, false);

      fork(new Runnable() {
         @Override
         public void run() {
            manager(1).stop();
         }
      }, false);

      eventually(new Condition() {
         @Override
         public boolean isSatisfied() throws Exception {
            List<Address> members = advancedCache(0).getRpcManager().getTransport().getMembers();
            log.trace("members = " + members);
            return members.size() == 1;
         }
      });

      log.trace("MultipleNodesLeavingTest.testMultipleLeaves");

      TestingUtil.blockUntilViewsReceived(60000, false, cache(0));
      TestingUtil.waitForRehashToComplete(cache(0));
      List<Address> caches = advancedCache(0).getDistributionManager().getConsistentHash().getMembers();
      log.tracef("caches = %s", caches);
      int size = caches.size();
      assert size == 1;
   }
}
