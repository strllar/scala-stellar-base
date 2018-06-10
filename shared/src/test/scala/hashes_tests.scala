package mytests

import org.scalatest._
import org.strllar.stellarbase.{Hex, RipeMD160, Sha256}

object JacksumSpec extends FlatSpec with Matchers {
  "SHA256" should "pass all" in {
    it should "correct" in {
      val DIGEST0 =
        "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD"
      //val mdo = new jonelo.jacksum.adapt.gnu.crypto.hash.Sha256;
      val mdo = new Sha256;
      mdo.update(0x61 toByte); // a
      mdo.update(0x62 toByte); // b
      mdo.update(0x63 toByte);
      Hex(mdo.digest).toString() shouldEqual DIGEST0

      val md = new Sha256;
      md.update(0x61 toByte); // a
      md.update(0x62 toByte); // b
      md.update(0x63 toByte);
      Hex(md.digest).toString() shouldEqual DIGEST0
    }
  }
  "RIPEMD160" should "pass all" in {
    it should "correct" in {
      val DIGEST0: String = "9C1185A5C5E9FC54612808977EE8F548B2258D31"
      //val mdo = new jonelo.jacksum.adapt.gnu.crypto.hash.RipeMD160
      val mdo = new RipeMD160
      Hex(mdo.digest).toString() shouldEqual DIGEST0
      val md = new RipeMD160
      Hex(md.digest).toString() shouldEqual DIGEST0

      md.reset()
      md.update("Rosetta Code".getBytes)
      Hex(md.digest).toString() shouldEqual "b3be159860842cebaa7174c8fff0aa9e50a5199f".toUpperCase
    }
  }
}