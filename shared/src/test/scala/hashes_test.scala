package mytests

import com.inthenow.zcheck.SpecLite
import org.strllar.stellarbase.{Hex, RipeMD160, Sha256}

object JacksumSpec extends SpecLite {
  "SHA256" should {
    "correct" in {
      val DIGEST0 =
        "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD"
      val mdo = new jonelo.jacksum.adapt.gnu.crypto.hash.Sha256;
      mdo.update(0x61); // a
      mdo.update(0x62); // b
      mdo.update(0x63);
      Hex(mdo.digest).toString() must_== DIGEST0

      val md = new Sha256;
      md.update(0x61); // a
      md.update(0x62); // b
      md.update(0x63);
      Hex(md.digest).toString() must_== DIGEST0
    }
  }
  "RIPEMD160" should {
    "correct" in {
      val DIGEST0: String = "9C1185A5C5E9FC54612808977EE8F548B2258D31"
      val mdo = new jonelo.jacksum.adapt.gnu.crypto.hash.RipeMD160
      Hex(mdo.digest).toString() must_== DIGEST0
      val md = new RipeMD160
      Hex(md.digest).toString() must_== DIGEST0
    }
  }
}