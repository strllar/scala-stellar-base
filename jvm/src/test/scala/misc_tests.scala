package mytests

import com.inthenow.zcheck.{SpecLite}

import org.strllar.stellarbase.{xdr}

object UnsortedSpec extends SpecLite {
  "Nothing" should {
    "be not wrong" in {
      xdr.Test.test()
    }
  }
}