package org.strllar.stellarbase

import java.nio.ByteOrder
import java.nio.ByteBuffer
import java.security.SecureRandom

import com.emstlk.nacl4s

import scala.util.{Try, Failure, Success}

class StrSeed(implicit network :Network) {
  val rawseed = new Array[Byte](32)
  def this(seedfeeds: Seq[Byte])(implicit network :Network) {
    this()
    seedfeeds.copyToArray(rawseed)
  }
  lazy val kp = nacl4s.SigningKeyPair(rawseed)

  override def toString() = network.keyFactory.formatSeed(this)

  def accountid = new StrAccountID(kp.publicKey)
}

object StrSeed {
  def parse(s :String)(implicit network :Network) = network.keyFactory.parseSeed(s)
}

class  StrAccountID(implicit network :Network) {
  val rawbytes = new Array[Byte](32)
  def this(bytes:  Seq[Byte])(implicit network :Network) {
    this()
    bytes.copyToArray(rawbytes)
  }
  override def toString() = network.keyFactory.formatAccountID(this)
}

object StrAccountID {
  def parse(s :String)(implicit network :Network) = network.keyFactory.parseAccountID(s)
}

object StrKey {

  val masterChant = "allmylifemyhearthasbeensearching"



  val crctab = (0 until 256).map { idx =>
    ((0 until 8) fold (idx << 8)) { (crc, _) =>
      val newcrc = crc << 1
      if ((newcrc & 0x10000) != 0)
        (newcrc ^ 0x1021)
      else newcrc
    }.toShort
  }.toArray

  def CRC16XMODEM(bytes:Seq[Byte]) = (bytes.foldLeft(0:Short)){(crc, b) =>
    val unsigned_crc = crc & 0xffff
    val unsigned_b = b & 0xff
    ((unsigned_crc << 8) ^ crctab((unsigned_crc >> 8) ^ unsigned_b)).toShort
  }

  def encodeCheck(version:Byte, data:Seq[Byte]) = {
    assume(data.size == 32)
    val buff  = ByteBuffer.allocate(1+32+2)
    val payload = (version +: data)
    val checksum = CRC16XMODEM(payload)

    buff.order(ByteOrder.LITTLE_ENDIAN)
    buff.put(payload.toArray)
    buff.putShort(checksum)

    buff.flip()
    val arr = new Array[Byte](buff.remaining())
    buff.get(arr)
    val base32eng = BaseN.base32
    base32eng.encode(arr)
  }

  def decodeCheck(version :Byte, s :String) = {
    val base32eng = BaseN.base32
    Try(base32eng.decode(s)) match {
      case Success(arr) if (arr.length == (1 + 32 + 2)) => {
        val crc = CRC16XMODEM(arr.slice(0, 33))
        val unsigned_crc = crc & 0xffff
        if (arr(0) == version) {
          if ((unsigned_crc & 0xff).toByte == arr(33) && (unsigned_crc >> 8).toByte == arr(34)) {
            Success(arr.slice(1, 33))
          }
          else Failure(new Exception("wrong crc"))
        }
        else Failure(new Exception("wrong version"))
      }
      case _ => Failure(new Exception("wrong format"))
    }
  }

  def legacyMaster()(implicit network :Network): StrSeed  = {
    new StrSeed(masterChant.getBytes)(network)
  }

  def master()(implicit network :Network): StrSeed  = {
    val md256 = new Sha256()
    md256.update(network.NETWORK_PASSPHRASE.getBytes)
    new StrSeed(md256.digest)
  }

  def random()(implicit network :Network): StrSeed  = {
    new StrSeed((new SecureRandom()).generateSeed(32))(network)
  }

}