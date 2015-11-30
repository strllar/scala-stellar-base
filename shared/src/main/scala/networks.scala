/**
 * Created by kring on 2015/11/2.
 */

package org.strllar.stellarbase

trait KeyFactory {
//  def toPUBKEY_ED25519()
//  def fromPUBKEY_ED25519()
//  def toSEED_ED25519()
//  def fromSEED_ED25519()
}

trait Network {
  val name :String
  val NETWORK_PASSPHRASE :String
  def keyFactory :KeyFactory

  val nativeCurrency :String
  val nativeUnit :Long

  val versionBytes :StrKey.VersionBytes
  val defaultVersionBytes = new StrKey.VersionBytes{
    val accountId :Byte = 0x30
    val seed :Byte = 0x90 toByte
  }
}

object Networks {
  case object XLM extends Network {
    override val NETWORK_PASSPHRASE = "Public Global Stellar Network ; September 2015"
    override val name = "Stellar Live"
    override def keyFactory = new KeyFactory{

    }
    override val nativeCurrency = "XLM"
    override val nativeUnit = 10000000L

    override implicit val versionBytes = defaultVersionBytes
  }
//  val XLMtest = new Network {
//    override val NETWORK_PASSPHRASE = "Test SDF Network ; September 2015"
//  }

  case object KLM extends Network {
    override val NETWORK_PASSPHRASE = "KLM is a Kilo of xLM; Strllar is an awesome copycat Stellar."
    override val name = "KLM Production"
    override def keyFactory = new KeyFactory{

    }
    override val nativeCurrency = "KLM"
    override val nativeUnit = 10000000000L

    override implicit val versionBytes = new StrKey.VersionBytes{
      val accountId :Byte = 0x68 toByte
      val seed :Byte = 0xb8 toByte
    }
  }

}
