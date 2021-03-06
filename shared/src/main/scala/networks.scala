/**
 * Created by kring on 2015/11/2.
 */

package org.strllar.stellarbase

import scala.util.Try

object KeyFactory {
  val KEY_TYPE_ED25519 = 0;
}

trait KeyFactory {
  def parseAccountID(s :String) :Try[StrAccountID]
  def formatAccountID(addr :StrAccountID) :String
  def parseSeed(s :String) :Try[StrSeed]
  def formatSeed(seed :StrSeed) :String
}

trait Network {
  val name :String
  val NETWORK_PASSPHRASE :String
  val keyFactory :KeyFactory

  val nativeCurrency :String
  val nativeUnit :Long

  lazy val networkId = {
    val md256 = new Sha256()
    md256.update(NETWORK_PASSPHRASE.getBytes)
    md256.digest.toVector
  }
}

object Networks {

  val defaultVersionBytes = new {
    val accountId :Byte = 0x30
    val seed :Byte = 0x90 toByte
  }

  implicit case object XLMLive extends Network {
    override val NETWORK_PASSPHRASE = "Public Global Stellar Network ; September 2015"
    override val name = "Stellar Live"
    override val keyFactory = new KeyFactory{
      override def parseAccountID(s :String) = StrKey.decodeCheck(defaultVersionBytes.accountId, s).map(new StrAccountID(_)(XLMLive.this))
      override def formatAccountID(addr :StrAccountID) = StrKey.encodeCheck(defaultVersionBytes.accountId, addr.rawbytes)
      override def parseSeed(s :String) = StrKey.decodeCheck(defaultVersionBytes.seed, s).map(new StrSeed(_)(XLMLive.this))
      override def formatSeed(seed :StrSeed) =  StrKey.encodeCheck(defaultVersionBytes.seed, seed.rawseed)
    }
    override val nativeCurrency = "XLM"
    override val nativeUnit = 10000000L
  }

  implicit case object XLMTestnet extends Network {
    override val NETWORK_PASSPHRASE = "Test SDF Network ; September 2015"
    override val name = "Stellar Testnet"
    override val keyFactory = new KeyFactory{
      override def parseAccountID(s :String) = StrKey.decodeCheck(defaultVersionBytes.accountId, s).map(new StrAccountID(_)(XLMTestnet.this))
      override def formatAccountID(addr :StrAccountID) = StrKey.encodeCheck(defaultVersionBytes.accountId, addr.rawbytes)
      override def parseSeed(s :String) = StrKey.decodeCheck(defaultVersionBytes.seed, s).map(new StrSeed(_)(XLMTestnet.this))
      override def formatSeed(seed :StrSeed) =  StrKey.encodeCheck(defaultVersionBytes.seed, seed.rawseed)
    }
    override val nativeCurrency = "XLM"
    override val nativeUnit = 10000000L
  }

  implicit case object KLMLive extends Network {
    val versionBytes = new {
      val accountId :Byte = 0x68 toByte
      val seed :Byte = 0xbe toByte
    }

    override val NETWORK_PASSPHRASE = "KLM is a Kilo of xLM; Strllar is an awesome copycat Stellar."
    override val name = "KLM Production"
    override val keyFactory = new KeyFactory{
      override def parseAccountID(s :String) = StrKey.decodeCheck(versionBytes.accountId, s).map(new StrAccountID(_)(KLMLive.this))
      override def formatAccountID(addr :StrAccountID) = StrKey.encodeCheck(versionBytes.accountId, addr.rawbytes)
      override def parseSeed(s :String) = StrKey.decodeCheck(versionBytes.seed, s).map(new StrSeed(_)(KLMLive.this))
      override def formatSeed(seed :StrSeed) =  StrKey.encodeCheck(versionBytes.seed, seed.rawseed)
    }
    override val nativeCurrency = "KLM"
    override val nativeUnit = 10000000000L
  }

}
