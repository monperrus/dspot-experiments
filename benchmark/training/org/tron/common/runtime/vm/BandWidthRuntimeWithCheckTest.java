/**
 * java-tron is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-tron is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.tron.common.runtime.vm;


import com.google.protobuf.Any;
import contractResult.SUCCESS;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.tron.common.application.TronApplicationContext;
import org.tron.common.runtime.TVMTestUtils;
import org.tron.common.runtime.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.tron.common.storage.DepositImpl;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.ReceiptCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.db.Manager;
import org.tron.core.db.TransactionTrace;
import org.tron.core.exception.ReceiptCheckErrException;
import org.tron.core.exception.TronException;
import org.tron.protos.Contract.TriggerSmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract;


/**
 * pragma solidity ^0.4.2;
 *
 * contract Fibonacci {
 *
 * event Notify(uint input, uint result);
 *
 * function fibonacci(uint number) constant returns(uint result) { if (number == 0) { return 0; }
 * else if (number == 1) { return 1; } else { uint256 first = 0; uint256 second = 1; uint256 ret =
 * 0; for(uint256 i = 2; i <= number; i++) { ret = first + second; first = second; second = ret; }
 * return ret; } }
 *
 * function fibonacciNotify(uint number) returns(uint result) { result = fibonacci(number);
 * Notify(number, result); } }
 */
public class BandWidthRuntimeWithCheckTest {
    public static final long totalBalance = 10000000000000L;

    private static String dbPath = "output_BandWidthRuntimeTest_test";

    private static String dbDirectory = "db_BandWidthRuntimeTest_test";

    private static String indexDirectory = "index_BandWidthRuntimeTest_test";

    private static AnnotationConfigApplicationContext context;

    private static Manager dbManager;

    private static String OwnerAddress = "TCWHANtDDdkZCTo2T2peyEq3Eg9c2XB7ut";

    private static String TriggerOwnerAddress = "TCSgeWapPJhCqgWRxXCKb6jJ5AgNWSGjPA";

    private static String TriggerOwnerTwoAddress = "TPMBUANrTwwQAPwShn7ZZjTJz1f3F8jknj";

    static {
        Args.setParam(new String[]{ "--output-directory", BandWidthRuntimeWithCheckTest.dbPath, "--storage-db-directory", BandWidthRuntimeWithCheckTest.dbDirectory, "--storage-index-directory", BandWidthRuntimeWithCheckTest.indexDirectory, "-w" }, "config-test-mainnet.conf");
        BandWidthRuntimeWithCheckTest.context = new TronApplicationContext(DefaultConfig.class);
    }

    @Test
    public void testSuccess() {
        try {
            byte[] contractAddress = createContract();
            AccountCapsule triggerOwner = BandWidthRuntimeWithCheckTest.dbManager.getAccountStore().get(Wallet.decodeFromBase58Check(BandWidthRuntimeWithCheckTest.TriggerOwnerAddress));
            long energy = triggerOwner.getEnergyUsage();
            long balance = triggerOwner.getBalance();
            TriggerSmartContract triggerContract = TVMTestUtils.createTriggerContract(contractAddress, "fibonacciNotify(uint256)", "7000", false, 0, Wallet.decodeFromBase58Check(BandWidthRuntimeWithCheckTest.TriggerOwnerAddress));
            Transaction transaction = Transaction.newBuilder().setRawData(raw.newBuilder().addContract(Contract.newBuilder().setParameter(Any.pack(triggerContract)).setType(ContractType.TriggerSmartContract)).setFeeLimit(1000000000)).build();
            TransactionCapsule trxCap = new TransactionCapsule(transaction);
            TransactionTrace trace = new TransactionTrace(trxCap, BandWidthRuntimeWithCheckTest.dbManager);
            BandWidthRuntimeWithCheckTest.dbManager.consumeBandwidth(trxCap, trace);
            BlockCapsule blockCapsule = null;
            DepositImpl deposit = DepositImpl.createRoot(BandWidthRuntimeWithCheckTest.dbManager);
            Runtime runtime = new org.tron.common.runtime.RuntimeImpl(trace, blockCapsule, deposit, new ProgramInvokeFactoryImpl());
            trace.init(blockCapsule);
            trace.exec();
            trace.finalization();
            triggerOwner = BandWidthRuntimeWithCheckTest.dbManager.getAccountStore().get(Wallet.decodeFromBase58Check(BandWidthRuntimeWithCheckTest.TriggerOwnerAddress));
            energy = (triggerOwner.getEnergyUsage()) - energy;
            balance = balance - (triggerOwner.getBalance());
            Assert.assertEquals(624668, trace.getReceipt().getEnergyUsageTotal());
            Assert.assertEquals(50000, energy);
            Assert.assertEquals(57466800, balance);
            Assert.assertEquals((624668 * (Constant.SUN_PER_ENERGY)), (balance + (energy * (Constant.SUN_PER_ENERGY))));
        } catch (TronException e) {
            Assert.assertNotNull(e);
        } catch (ReceiptCheckErrException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testSuccessNoBandWidth() {
        try {
            byte[] contractAddress = createContract();
            TriggerSmartContract triggerContract = TVMTestUtils.createTriggerContract(contractAddress, "fibonacciNotify(uint256)", "50", false, 0, Wallet.decodeFromBase58Check(BandWidthRuntimeWithCheckTest.TriggerOwnerTwoAddress));
            Transaction transaction = Transaction.newBuilder().setRawData(raw.newBuilder().addContract(Contract.newBuilder().setParameter(Any.pack(triggerContract)).setType(ContractType.TriggerSmartContract)).setFeeLimit(1000000000)).build();
            TransactionCapsule trxCap = new TransactionCapsule(transaction);
            trxCap.setResultCode(SUCCESS);
            TransactionTrace trace = new TransactionTrace(trxCap, BandWidthRuntimeWithCheckTest.dbManager);
            BandWidthRuntimeWithCheckTest.dbManager.consumeBandwidth(trxCap, trace);
            long bandWidth = (trxCap.getSerializedSize()) + (Constant.MAX_RESULT_SIZE_IN_TX);
            BlockCapsule blockCapsule = null;
            DepositImpl deposit = DepositImpl.createRoot(BandWidthRuntimeWithCheckTest.dbManager);
            Runtime runtime = new org.tron.common.runtime.RuntimeImpl(trace, blockCapsule, deposit, new ProgramInvokeFactoryImpl());
            trace.init(blockCapsule);
            trace.exec();
            trace.finalization();
            trace.check();
            AccountCapsule triggerOwnerTwo = BandWidthRuntimeWithCheckTest.dbManager.getAccountStore().get(Wallet.decodeFromBase58Check(BandWidthRuntimeWithCheckTest.TriggerOwnerTwoAddress));
            long balance = triggerOwnerTwo.getBalance();
            ReceiptCapsule receipt = trace.getReceipt();
            Assert.assertNull(getRuntimeError());
            Assert.assertEquals(bandWidth, receipt.getNetUsage());
            Assert.assertEquals(6118, receipt.getEnergyUsageTotal());
            Assert.assertEquals(6118, receipt.getEnergyUsage());
            Assert.assertEquals(0, receipt.getEnergyFee());
            Assert.assertEquals(BandWidthRuntimeWithCheckTest.totalBalance, balance);
        } catch (TronException e) {
            Assert.assertNotNull(e);
        } catch (ReceiptCheckErrException e) {
            Assert.assertNotNull(e);
        }
    }
}
