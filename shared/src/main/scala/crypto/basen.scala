/**
 * Created by kring on 2015/7/31.
 */
package org.strllar.stellarbase

import UnsignedOps.unsignedValue
import scala.annotation.tailrec

object BaseN {
  //// https://tools.ietf.org/rfc/rfc4648.txt
  //val base1Spec =	("base1/BINARY", 1, "01", null)
  val base16Spec =	("base16/HEX", 4, "0123456789ABCDEF", None)
  val base32Spec = ("base32", 5, "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567", Some('='))
  val base32hexSpec = ("base32hex", 5, "0123456789ABCDEFGHIJKLMNOPQRSTUV", Some('='))
  val base64Spec = ("base64", 6, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", Some('='))
  val base64urlSpec = ("base64", 6, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_", Some('='))
  //val base256Spec = ("base256/raw", 8, Array.iterate(0, 256), null)

  //for baseN in RFC4648 where N = (1<<bitsPerChar) and bitsPerChar < 8
  //we have bitsPerChar * charsPerChunk = LCM(bitsPerChar,8) < 64 = bitsPerLong
  //these functions are for dealing with BigEndian bits in chunk of RFC4648
  //we use a trick that stores bits in BigEndian order into Long in BigEndian byte order
  //to avoid duplicated Bit Reverse operations
  type SymChar = Byte

  private[this] def packToLong(input :Seq[SymChar], bitsPerChar :Int) :Long = {
    assert(input.length <= 8 && input.length * bitsPerChar < 64)
    input.foldLeft(0L)((l, x) => (l << bitsPerChar) | unsignedValue(x))
  }

  private[this] def unpackToChars(input :Long, bitsPerChar :Int, charsPerChunk :Int) :List[SymChar] = {
    assert(bitsPerChar <= 8 && bitsPerChar*charsPerChunk < 64)
    val mask :Long = (-1L) >>> (64 - bitsPerChar)
    @tailrec def recursiveUnpack(i :Long, c :Int, ret :List[Byte]) :List[Byte] = {
      if (c == 0) ret
      else recursiveUnpack(i >>> bitsPerChar, c-1, (i & mask).toByte :: ret)
    }
    recursiveUnpack(input, charsPerChunk, Nil)
  }
  //treat Byte as base256 Symbol
  private[this] def packToLong(input :Seq[Byte]) :Long = packToLong(input, 8)
  private[this] def unpackToBytes(input :Long, bytePerChunk :Int) :List[Byte] = unpackToChars(input, 8, bytePerChunk)

  def createCodec[T](spec :T) = {
    val (name :String, bitsPerChar :Int, chars :String, padding :Option[Char]) = spec
    new  {
      val revchars = chars.zipWithIndex.toMap

      val gcd = Math.min(8, Integer.lowestOneBit(bitsPerChar));
      val bytesPerChunk :Int = bitsPerChar / gcd
      val charsPerChunk :Int = 8 / gcd

      val padlen = (0 until bytesPerChunk).map(x => (x.toByte -> (x * 8 / bitsPerChar).toByte)).toMap
      val revpadlen = padlen.map(x => (x._2 -> x._1)).toMap

      def encode(bytes :Seq[Byte]) = {
        val bytesPad = ((bytesPerChunk - bytes.length % bytesPerChunk) % bytesPerChunk).toByte
        val charsPad = padlen(bytesPad)

        val padded = bytes.padTo(bytes.length + bytesPad, 0:Byte).grouped(bytesPerChunk).flatMap(x => {
          unpackToChars(packToLong(x), bitsPerChar, charsPerChunk)
        }).toSeq
        (padded.dropRight(charsPad).map(b => chars(b)) ++ Seq.fill(charsPad)(padding.get)).mkString
      }
      def decode(chars :String) = {
        assert(chars.length % charsPerChunk == 0)
        val padded = chars.map(b => revchars.applyOrElse(b, (_:Char) => 0).toByte).grouped(charsPerChunk).flatMap(x => {
          unpackToBytes(packToLong(x, bitsPerChar), bytesPerChunk)
        }).toSeq
        val charsPad = chars.takeRight(charsPerChunk-1).reverse.span(Some(_) == padding)._1.length.toByte
        padded.dropRight(revpadlen(charsPad)).toArray
      }
    }
  }

  val base16 = createCodec(base16Spec)
  val base32 = createCodec(base32Spec)
  val base32hex = createCodec(base32hexSpec)
  val base64 = createCodec(base64Spec)
  val base64url = createCodec(base64urlSpec)

  class BaseNString(val ms :Array[Byte], val nbit :Int) {
    override def equals(other :Any): Boolean = {
        false
      }
  }

}

//object Hex {
//  import BaseN.BaseNString
//
//  //def normalize(sep:Option[Char] = ' ', cap:Boolean = false) = ""
//
//  def apply(s:String) = new BaseNString(ApacheHex.decodeHex(s.toCharArray), 4)
//  def apply(ch :Int*) = new BaseNString(ch.map(_.toByte).toArray, 4)
//  def apply(bs :Array[Byte]) = new BaseNString(bs, 4)
//}
//
//object Lit {
//  import BaseN.BaseNString
//  lazy val base32eng = new Base32
//
//  def apply(s:String) = new BaseNString(s.getBytes, 8) {
//    def toBase32Raw = base32eng.encodeToString(ms)
//    def toBase32 = new BaseNString(ms, 5)
//
//    def asBase32 = Hex(base32eng.decode(ms))
//  }
//}