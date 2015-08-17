package org.strllar.stellarbase

import java.nio.{ByteOrder, ByteBuffer}
import akka.util.{ByteString, ByteStringBuilder}
import java.security.SecureRandom
import com.iwebpp.crypto.TweetNacl

import org.apache.commons.codec.binary.Base32

case class StrSeed(seedfeeds: Array[Byte]) {
  val rawseed = seedfeeds.padTo(32, 0:Byte).take(32).toArray;
  lazy val kp = TweetNacl.Signature.keyPair_fromSeed(rawseed)

  override def toString() = {
    StrKey.encodeCheck(StrKey.versionBytes.seed, rawseed)
  }
  def address = StrAddress(kp.getPublicKey)
}

case class  StrAddress(byteBuffer:  Array[Byte]) {
  override def toString() = {
    StrKey.encodeCheck(StrKey.versionBytes.accountId, byteBuffer)
  }
}

object StrKey {
  //"GCEZWKCA5VLDNRLN3RPRJMRZOX3Z6G5CHCGSNFHEYVXM3XOJMDS674JZ"
  //"SBQWY3DNPFWGSZTFNV4WQZLBOJ2GQYLTMJSWK3TTMVQXEY3INFXGO52X"
  val masterChant = "allmylifemyhearthasbeensearching"

  val versionBytes = new {
    val accountId = 0x30 toByte
    val seed = 0x90 toByte
  }

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
    val buff  = new ByteStringBuilder
    val payload = (version +: data)
    val checksum = CRC16XMODEM(payload)
    buff ++= payload
    buff.putShort(checksum)(ByteOrder.LITTLE_ENDIAN)

    val base32eng = new Base32
    base32eng.encodeToString(buff.result().toArray)
  }

  def master(): StrSeed  = {
    StrSeed(masterChant.getBytes)
  }

  def random(): StrSeed  = {
    StrSeed((new SecureRandom()).generateSeed(32))
  }

}