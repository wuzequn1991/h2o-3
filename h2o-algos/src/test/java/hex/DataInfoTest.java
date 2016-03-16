package hex;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import water.Key;
import water.MRTask;
import water.TestUtil;
import water.fvec.Chunk;
import water.fvec.Frame;
import water.fvec.InteractionWrappedVec;
import water.fvec.Vec;


// test cases:
// skipMissing = TRUE/FALSE
// useAllLevels = TRUE/FALSE
// limit enums
// (dont) standardize predictor columns

// data info tests with interactions
public class DataInfoTest extends TestUtil {

  @BeforeClass static public void setup() {  stall_till_cloudsize(1); }


  @Test public void testAirlines1() { // just test that it works at all
    Frame fr = parse_test_file(Key.make("a.hex"), "smalldata/airlines/allyears2k_headers.zip");
    try {
      DataInfo dinfo = new DataInfo(
              fr.clone(),  // train
              null,        // valid
              1,           // num responses
              true,        // use all factor levels
              DataInfo.TransformType.STANDARDIZE,  // predictor transform
              DataInfo.TransformType.NONE,         // response  transform
              true,        // skip missing
              false,       // impute missing
              false,       // missing bucket
              false,       // weight
              false,       // offset
              false,       // fold
              DataInfo.InteractionPair.generatePairwiseInteractionsFromList(8, 16, 2)  // interactions
      );
      dinfo.dropInteractions();
      dinfo.remove();
    } finally {
      fr.delete();
    }
  }


  @Test public void testAirlines2() {
    Frame fr = parse_test_file(Key.make("a.hex"), "smalldata/airlines/allyears2k_headers.zip");
    try {
      Frame interactions = DataInfo.makeInteractions(fr,false,DataInfo.InteractionPair.generatePairwiseInteractionsFromList(8, 16, 2),true,true);
      int len=0;
      for(Vec v: interactions.vecs()) len += ((InteractionWrappedVec)v).expandedLength();
      interactions.delete();
      Assert.assertTrue(len==290+132+10);

      DataInfo dinfo__noInteractions = new DataInfo(
              fr.clone(),  // train
              null,        // valid
              1,           // num responses
              true,        // use all factor levels
              DataInfo.TransformType.STANDARDIZE,  // predictor transform
              DataInfo.TransformType.NONE,         // response  transform
              true,        // skip missing
              false,       // impute missing
              false,       // missing bucket
              false,       // weight
              false,       // offset
              false,       // fold
              null
      );

      System.out.println(dinfo__noInteractions.fullN());
      System.out.println(dinfo__noInteractions.numNums());


      DataInfo dinfo__withInteractions = new DataInfo(
              fr.clone(),  // train
              null,        // valid
              1,           // num responses
              true,        // use all factor levels
              DataInfo.TransformType.STANDARDIZE,  // predictor transform
              DataInfo.TransformType.NONE,         // response  transform
              true,        // skip missing
              false,       // impute missing
              false,       // missing bucket
              false,       // weight
              false,       // offset
              false,       // fold
              DataInfo.InteractionPair.generatePairwiseInteractionsFromList(8, 16, 2)  // interactions
      );
      System.out.println(dinfo__withInteractions.fullN());
      Assert.assertTrue(dinfo__withInteractions.fullN() == dinfo__noInteractions.fullN() + len);
      dinfo__withInteractions.dropInteractions();
      dinfo__noInteractions.remove();
      dinfo__withInteractions.remove();
    } finally {
      fr.delete();
    }
  }

  @Test public void testAirlines3() {
    Frame fr = parse_test_file(Key.make("a.hex"), "smalldata/airlines/allyears2k_headers.zip");
    try {
      Frame interactions = DataInfo.makeInteractions(fr,false,DataInfo.InteractionPair.generatePairwiseInteractionsFromList(8, 16, 2),false,true);
      int len=0;
      for(Vec v: interactions.vecs()) len += ((InteractionWrappedVec)v).expandedLength();
      interactions.delete();
      Assert.assertTrue(len==426);

      DataInfo dinfo__noInteractions = new DataInfo(
              fr.clone(),  // train
              null,        // valid
              1,           // num responses
              false,        // use all factor levels
              DataInfo.TransformType.STANDARDIZE,  // predictor transform
              DataInfo.TransformType.NONE,         // response  transform
              true,        // skip missing
              false,       // impute missing
              false,       // missing bucket
              false,       // weight
              false,       // offset
              false,       // fold
              null
      );

      System.out.println(dinfo__noInteractions.fullN());

      DataInfo dinfo__withInteractions = new DataInfo(
              fr.clone(),  // train
              null,        // valid
              1,           // num responses
              false,        // use all factor levels
              DataInfo.TransformType.STANDARDIZE,  // predictor transform
              DataInfo.TransformType.NONE,         // response  transform
              true,        // skip missing
              false,       // impute missing
              false,       // missing bucket
              false,       // weight
              false,       // offset
              false,       // fold
              DataInfo.InteractionPair.generatePairwiseInteractionsFromList(8, 16, 2)  // interactions
      );
      System.out.println(dinfo__withInteractions.fullN());
      Assert.assertTrue(dinfo__withInteractions.fullN() == dinfo__noInteractions.fullN() + len);
      dinfo__withInteractions.dropInteractions();
      dinfo__noInteractions.remove();
      dinfo__withInteractions.remove();
    } finally {
      fr.delete();
    }
  }




  @Test public void testIris1() {  // test that getting sparseRows and denseRows produce the same results
    Frame fr = parse_test_file(Key.make("a.hex"), "smalldata/iris/iris_wheader.csv");
    fr.swap(1,4);
    DataInfo.InteractionPair[] ips = DataInfo.InteractionPair.generatePairwiseInteractionsFromList(0, 1);

    try {
      final DataInfo di = new DataInfo(
              fr.clone(),  // train
              null,        // valid
              1,           // num responses
              true,        // use all factor levels
              DataInfo.TransformType.NONE,  // predictor transform
              DataInfo.TransformType.NONE,  // response  transform
              true,        // skip missing
              false,       // impute missing
              false,       // missing bucket
              false,       // weight
              false,       // offset
              false,       // fold
              ips          // interactions
      );

      new MRTask() {
        @Override public void map(Chunk[] cs) {
          DataInfo.Row[] sparseRows = di.extractSparseRows(cs);
          for(int i=0;i<cs[0]._len;++i) {
            DataInfo.Row r=di.newDenseRow();
            di.extractDenseRow(cs,i,r);
            for(int j=0;j<di.fullN();++j)
              if( r.get(j) != sparseRows[i].get(j) )
                throw new RuntimeException("Row mismatch on row " + i);
          }
        }
      }.doAll(di._adaptedFrame);
      di.dropInteractions();
      di.remove();
    } finally {
      fr.delete();
    }
  }
}
