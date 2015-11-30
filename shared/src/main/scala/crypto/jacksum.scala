package org.strllar.stellarbase

// ----------------------------------------------------------------------------
// $Id: IMessageDigest.java,v 1.9 2002/11/07 17:17:45 raif Exp $
//
// Copyright (C) 2001, 2002, Free Software Foundation, Inc.
//
// This file is part of GNU Crypto.
//
// GNU Crypto is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.
//
// GNU Crypto is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; see the file COPYING.  If not, write to the
//
//    Free Software Foundation Inc.,
//    59 Temple Place - Suite 330,
//    Boston, MA 02111-1307
//    USA
//
// Linking this library statically or dynamically with other modules is
// making a combined work based on this library.  Thus, the terms and
// conditions of the GNU General Public License cover the whole
// combination.
//
// As a special exception, the copyright holders of this library give
// you permission to link this library with independent modules to
// produce an executable, regardless of the license terms of these
// independent modules, and to copy and distribute the resulting
// executable under terms of your choice, provided that you also meet,
// for each linked independent module, the terms and conditions of the
// license of that module.  An independent module is a module which is
// not derived from or based on this library.  If you modify this
// library, you may extend this exception to your version of the
// library, but you are not obligated to do so.  If you do not wish to
// do so, delete this exception statement from your version.
// ----------------------------------------------------------------------------
/**
  * <p>The basic visible methods of any hash algorithm.</p>
  *
  * <p>A hash (or message digest) algorithm produces its output by iterating a
  * basic compression function on blocks of data.</p>
  *
  * @version $Revision: 1.9 $
  */
trait IMessageDigest {
  /**
    * <p>Returns the canonical name of this algorithm.</p>
    *
    * @return the canonical name of this instance.
    */
  def name: String

  /**
    * <p>Returns the output length in bytes of this message digest algorithm.</p>
    *
    * @return the output length in bytes of this message digest algorithm.
    */
  def hashSize: Int

  /**
    * <p>Returns the algorithm's (inner) block size in bytes.</p>
    *
    * @return the algorithm's inner block size in bytes.
    */
  def blockSize: Int

  /**
    * <p>Continues a message digest operation using the input byte.</p>
    *
    * @param b the input byte to digest.
    */
  def update(b: Byte)

  /**
    * <p>Continues a message digest operation, by filling the buffer, processing
    * data in the algorithm's HASH_SIZE-bit block(s), updating the context and
    * count, and buffering the remaining bytes in buffer for the next
    * operation.</p>
    *
    * @param in the input block.
    */
  def update(in: IndexedSeq[Byte])

  /**
    * <p>Completes the message digest by performing final operations such as
    * padding and resetting the instance.</p>
    *
    * @return the array of bytes representing the hash value.
    */
  def digest: Array[Byte]

  /**
    * <p>Resets the current context of this instance clearing any eventually cached
    * intermediary values.</p>
    */
  def reset()
}

// ----------------------------------------------------------------------------
// $Id: BaseHash.java,v 1.8 2002/11/07 17:17:45 raif Exp $
//
// Copyright (C) 2001, 2002, Free Software Foundation, Inc.
//
// This file is part of GNU Crypto.
//
// GNU Crypto is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.
//
// GNU Crypto is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; see the file COPYING.  If not, write to the
//
//    Free Software Foundation Inc.,
//    59 Temple Place - Suite 330,
//    Boston, MA 02111-1307
//    USA
//
// Linking this library statically or dynamically with other modules is
// making a combined work based on this library.  Thus, the terms and
// conditions of the GNU General Public License cover the whole
// combination.
//
// As a special exception, the copyright holders of this library give
// you permission to link this library with independent modules to
// produce an executable, regardless of the license terms of these
// independent modules, and to copy and distribute the resulting
// executable under terms of your choice, provided that you also meet,
// for each linked independent module, the terms and conditions of the
// license of that module.  An independent module is a module which is
// not derived from or based on this library.  If you modify this
// library, you may extend this exception to your version of the
// library, but you are not obligated to do so.  If you do not wish to
// do so, delete this exception statement from your version.
// ----------------------------------------------------------------------------
/**
  * <p>A base abstract class to facilitate hash implementations.</p>
  *
  * @version $Revision: 1.8 $
  */
abstract class BaseHash(val name: String, val hashSize: Int, val blockSize: Int) extends IMessageDigest {
  val buffer: Array[Byte] = new Array[Byte](blockSize)
  var count: Long = 0L
  resetContext()


  override def update(b: Byte) {
    val i: Int = (count % blockSize).toInt
    count += 1
    buffer(i) = b
    if (i == (blockSize - 1)) {
      transform(buffer, 0)
    }
  }

  override def update(b: IndexedSeq[Byte]) {
    val len = b.length
    var n: Int = (count % blockSize).toInt
    count += len
    val partLen: Int = blockSize - n
    var i: Int = 0
    if (len >= partLen) {
      b.copyToArray(buffer, n, partLen)
      transform(buffer, 0);
      {
        i = partLen
        while (i + blockSize - 1 < len) {
          {
            transform(b, i)
          }
          i += blockSize
        }
      }
      n = 0
    }
    if (i < len) {
      b.drop(i).copyToArray(buffer, n, len - i)
    }
  }

  override def digest: Array[Byte] = {
    val tail: Array[Byte] = padBuffer
    update(tail)
    val result: Array[Byte] = getResult
    reset
    return result
  }

  override def reset() {
    count = 0L;
    {
      var i: Int = 0
      while (i < blockSize) {
        buffer(({
          i += 1; i - 1
        })) = 0
      }
    }
    resetContext
  }

  /**
    * <p>Returns the byte array to use as padding before completing a hash
    * operation.</p>
    *
    * @return the bytes to pad the remaining bytes in the buffer before
    *         completing a hash operation.
    */
  protected def padBuffer: Array[Byte]

  /**
    * <p>Constructs the result from the contents of the current context.</p>
    *
    * @return the output of the completed hash operation.
    */
  protected def getResult: Array[Byte]

  /** Resets the instance for future re-use. */
  protected def resetContext()

  /**
    * <p>The block digest transformation per se.</p>
    *
    * @param in the <i>blockSize</i> long block, as an array of bytes to digest.
    * @param offset the index where the data to digest is located within the
    *               input buffer.
    */
  protected def transform(in: IndexedSeq[Byte], offset: Int)
}

// ----------------------------------------------------------------------------
// $Id: Sha256.java,v 1.1 2003/09/26 23:40:15 raif Exp $
//
// Copyright (C) 2003 Free Software Foundation, Inc.
//
// This file is part of GNU Crypto.
//
// GNU Crypto is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.
//
// GNU Crypto is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; see the file COPYING.  If not, write to the
//
//    Free Software Foundation Inc.,
//    59 Temple Place - Suite 330,
//    Boston, MA 02111-1307
//    USA
//
// Linking this library statically or dynamically with other modules is
// making a combined work based on this library.  Thus, the terms and
// conditions of the GNU General Public License cover the whole
// combination.
//
// As a special exception, the copyright holders of this library give
// you permission to link this library with independent modules to
// produce an executable, regardless of the license terms of these
// independent modules, and to copy and distribute the resulting
// executable under terms of your choice, provided that you also meet,
// for each linked independent module, the terms and conditions of the
// license of that module.  An independent module is a module which is
// not derived from or based on this library.  If you modify this
// library, you may extend this exception to your version of the
// library, but you are not obligated to do so.  If you do not wish to
// do so, delete this exception statement from your version.
// ----------------------------------------------------------------------------

/**
  * <p>Implementation of SHA2 [SHA-256] per the IETF Draft Specification.</p>
  *
  * <p>References:</p>
  * <ol>
  * <li><a href="http://ftp.ipv4.heanet.ie/pub/ietf/internet-drafts/draft-ietf-ipsec-ciph-aes-cbc-03.txt">
  * Descriptions of SHA-256, SHA-384, and SHA-512</a>,</li>
  * <li>http://csrc.nist.gov/cryptval/shs/sha256-384-512.pdf</li>
  * </ol>
  *
  * @version $Revision: 1.1 $
  */
object Sha256 {
  private val k: Array[Int] = Array(0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5, 0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174, 0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2)
  private val BLOCK_SIZE: Int = 64
  private val w: Array[Int] = new Array[Int](64)
  /** caches the result of the correctness test, once executed. */
  private var valid: Boolean = false

  def G(hh0: Int, hh1: Int, hh2: Int, hh3: Int, hh4: Int, hh5: Int, hh6: Int, hh7: Int, in: Array[Byte], offset: Int): Array[Int] = {
    return sha(hh0, hh1, hh2, hh3, hh4, hh5, hh6, hh7, in, offset)
  }

  private def sha(hh0: Int, hh1: Int, hh2: Int, hh3: Int, hh4: Int, hh5: Int, hh6: Int, hh7: Int, in: IndexedSeq[Byte], offset: Int): Array[Int] = {
    var A: Int = hh0
    var B: Int = hh1
    var C: Int = hh2
    var D: Int = hh3
    var E: Int = hh4
    var F: Int = hh5
    var G: Int = hh6
    var H: Int = hh7
    var r: Int = 0
    var T: Int = 0
    var T2: Int = 0
    var offset_var = offset;
    {
      r = 0
      while (r < 16) {
        {
          w(r) = in(({
            offset_var += 1; offset_var - 1
          })) << 24 | (in(({
            offset_var += 1; offset_var - 1
          })) & 0xFF) << 16 | (in(({
            offset_var += 1; offset_var - 1
          })) & 0xFF) << 8 | (in(({
            offset_var += 1; offset_var - 1
          })) & 0xFF)
        }
        ({
          r += 1; r - 1
        })
      }
    }
    {
      r = 16
      while (r < 64) {
        {
          T = w(r - 2)
          T2 = w(r - 15)
          w(r) = (((T >>> 17) | (T << 15)) ^ ((T >>> 19) | (T << 13)) ^ (T >>> 10)) + w(r - 7) + (((T2 >>> 7) | (T2 << 25)) ^ ((T2 >>> 18) | (T2 << 14)) ^ (T2 >>> 3)) + w(r - 16)
        }
        ({
          r += 1; r - 1
        })
      }
    }
    {
      r = 0
      while (r < 64) {
        {
          T = H + (((E >>> 6) | (E << 26)) ^ ((E >>> 11) | (E << 21)) ^ ((E >>> 25) | (E << 7))) + ((E & F) ^ (~E & G)) + k(r) + w(r)
          T2 = (((A >>> 2) | (A << 30)) ^ ((A >>> 13) | (A << 19)) ^ ((A >>> 22) | (A << 10))) + ((A & B) ^ (A & C) ^ (B & C))
          H = G
          G = F
          F = E
          E = D + T
          D = C
          C = B
          B = A
          A = T + T2
        }
        ({
          r += 1; r - 1
        })
      }
    }
    return Array[Int](hh0 + A, hh1 + B, hh2 + C, hh3 + D, hh4 + E, hh5 + F, hh6 + G, hh7 + H)
  }
}

class Sha256( private var h0: Int,
               private var h1: Int,
               private var h2: Int,
               private var h3: Int,
               private var h4: Int,
               private var h5: Int,
               private var h6: Int,
               private var h7: Int
            ) extends BaseHash("sha-256", 32, Sha256.BLOCK_SIZE) {

  def this() {
    /** 256-bit interim result. */
    //h0-h7
    this(0, 0, 0, 0, 0, 0, 0, 0);
  }

  override protected def transform(in: IndexedSeq[Byte], offset: Int) {
    val result: Array[Int] = Sha256.sha(h0, h1, h2, h3, h4, h5, h6, h7, in, offset)
    h0 = result(0)
    h1 = result(1)
    h2 = result(2)
    h3 = result(3)
    h4 = result(4)
    h5 = result(5)
    h6 = result(6)
    h7 = result(7)
  }

  protected def padBuffer: Array[Byte] = {
    val n: Int = (count % Sha256.BLOCK_SIZE).asInstanceOf[Int]
    var padding: Int = if ((n < 56)) (56 - n) else (120 - n)
    val result: Array[Byte] = new Array[Byte](padding + 8)
    result(0) = 0x80.toByte
    val bits: Long = count << 3
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 56).toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 48).toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 40).toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 32).toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 24).toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 16).toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 8).toByte
    result(padding) = bits.toByte
    return result
  }

  protected def getResult: Array[Byte] = {
    return Array[Byte]((h0 >>> 24).toByte, (h0 >>> 16).toByte, (h0 >>> 8).toByte, h0.toByte, (h1 >>> 24).toByte, (h1 >>> 16).toByte, (h1 >>> 8).toByte, h1.toByte, (h2 >>> 24).toByte, (h2 >>> 16).toByte, (h2 >>> 8).toByte, h2.toByte, (h3 >>> 24).toByte, (h3 >>> 16).toByte, (h3 >>> 8).toByte, h3.toByte, (h4 >>> 24).toByte, (h4 >>> 16).toByte, (h4 >>> 8).toByte, h4.toByte, (h5 >>> 24).toByte, (h5 >>> 16).toByte, (h5 >>> 8).toByte, h5.toByte, (h6 >>> 24).toByte, (h6 >>> 16).toByte, (h6 >>> 8).toByte, h6.toByte, (h7 >>> 24).toByte, (h7 >>> 16).toByte, (h7 >>> 8).toByte, h7.toByte)
  }

  override protected def resetContext() {
    h0 = 0x6a09e667
    h1 = 0xbb67ae85
    h2 = 0x3c6ef372
    h3 = 0xa54ff53a
    h4 = 0x510e527f
    h5 = 0x9b05688c
    h6 = 0x1f83d9ab
    h7 = 0x5be0cd19
  }
}

// ----------------------------------------------------------------------------
// $Id: RipeMD160.java,v 1.7 2002/11/07 17:17:45 raif Exp $
//
// Copyright (C) 2001, 2002, Free Software Foundation, Inc.
//
// This file is part of GNU Crypto.
//
// GNU Crypto is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.
//
// GNU Crypto is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; see the file COPYING.  If not, write to the
//
//    Free Software Foundation Inc.,
//    59 Temple Place - Suite 330,
//    Boston, MA 02111-1307
//    USA
//
// Linking this library statically or dynamically with other modules is
// making a combined work based on this library.  Thus, the terms and
// conditions of the GNU General Public License cover the whole
// combination.
//
// As a special exception, the copyright holders of this library give
// you permission to link this library with independent modules to
// produce an executable, regardless of the license terms of these
// independent modules, and to copy and distribute the resulting
// executable under terms of your choice, provided that you also meet,
// for each linked independent module, the terms and conditions of the
// license of that module.  An independent module is a module which is
// not derived from or based on this library.  If you modify this
// library, you may extend this exception to your version of the
// library, but you are not obligated to do so.  If you do not wish to
// do so, delete this exception statement from your version.
// ----------------------------------------------------------------------------

/**
  * <p>RIPEMD-160 is a 160-bit message digest.</p>
  *
  * <p>References:</p>
  *
  * <ol>
  * <li><a href="http://www.esat.kuleuven.ac.be/~bosselae/ripemd160.html">
  * RIPEMD160</a>: A Strengthened Version of RIPEMD.<br>
  * Hans Dobbertin, Antoon Bosselaers and Bart Preneel.</li>
  * </ol>
  *
  * @version $Revision: 1.7 $
  */
object RipeMD160 {
  private val BLOCK_SIZE: Int = 64
  private val DIGEST0: String = "9C1185A5C5E9FC54612808977EE8F548B2258D31"
  private val R: Array[Int] = Array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 7, 4, 13, 1, 10, 6, 15, 3, 12, 0, 9, 5, 2, 14, 11, 8, 3, 10, 14, 4, 9, 15, 8, 1, 2, 7, 0, 6, 13, 11, 5, 12, 1, 9, 11, 10, 0, 8, 12, 4, 13, 3, 7, 15, 14, 5, 6, 2, 4, 0, 5, 9, 7, 12, 2, 10, 14, 1, 3, 8, 11, 6, 15, 13)
  private val Rp: Array[Int] = Array(5, 14, 7, 0, 9, 2, 11, 4, 13, 6, 15, 8, 1, 10, 3, 12, 6, 11, 3, 7, 0, 13, 5, 10, 14, 15, 8, 12, 4, 9, 1, 2, 15, 5, 1, 3, 7, 14, 6, 9, 11, 8, 12, 2, 10, 0, 4, 13, 8, 6, 4, 1, 3, 11, 15, 0, 5, 12, 2, 13, 9, 7, 10, 14, 12, 15, 10, 4, 1, 5, 8, 7, 6, 2, 13, 14, 0, 3, 9, 11)
  private val S: Array[Int] = Array(11, 14, 15, 12, 5, 8, 7, 9, 11, 13, 14, 15, 6, 7, 9, 8, 7, 6, 8, 13, 11, 9, 7, 15, 7, 12, 15, 9, 11, 7, 13, 12, 11, 13, 6, 7, 14, 9, 13, 15, 14, 8, 13, 6, 5, 12, 7, 5, 11, 12, 14, 15, 14, 15, 9, 8, 9, 14, 5, 6, 8, 6, 5, 12, 9, 15, 5, 11, 6, 8, 13, 12, 5, 12, 13, 14, 11, 8, 5, 6)
  private val Sp: Array[Int] = Array(8, 9, 9, 11, 13, 15, 15, 5, 7, 7, 8, 11, 14, 14, 12, 6, 9, 13, 15, 7, 12, 8, 9, 11, 7, 7, 12, 7, 6, 15, 13, 11, 9, 7, 15, 11, 8, 6, 6, 14, 12, 13, 5, 14, 13, 13, 7, 5, 15, 5, 8, 11, 14, 14, 6, 14, 6, 9, 12, 9, 12, 5, 15, 8, 8, 5, 12, 9, 12, 5, 14, 6, 8, 13, 6, 5, 15, 13, 11, 11)
  /** caches the result of the correctness test, once executed. */
  private var valid: Boolean = false
}

class RipeMD160(private var h0: Int,
                private var h1: Int,
                private var h2: Int,
                private var h3: Int,
                private var h4: Int
               ) extends BaseHash("ripemd160", 20, RipeMD160.BLOCK_SIZE) {
  /** 512 bits work buffer = 16 x 32-bit words */
  private var X: Array[Int] = new Array[Int](16)

  /** Trivial 0-arguments constructor. */
  def this() {
    /** 160-bit h0, h1, h2, h3, h4 (interim result) */
    this(0, 0, 0, 0, 0)
  }

  override protected def transform(in: IndexedSeq[Byte], offset: Int) {
    var A: Int = 0
    var B: Int = 0
    var C: Int = 0
    var D: Int = 0
    var E: Int = 0
    var Ap: Int = 0
    var Bp: Int = 0
    var Cp: Int = 0
    var Dp: Int = 0
    var Ep: Int = 0
    var T: Int = 0
    var s: Int = 0
    var i: Int = 0
    var offset_var = offset;
    {
      i = 0
      while (i < 16) {
        {
          X(i) = (in(({
            offset_var += 1; offset_var - 1
          })) & 0xFF) | (in(({
            offset_var += 1; offset_var - 1
          })) & 0xFF) << 8 | (in(({
            offset_var += 1; offset_var - 1
          })) & 0xFF) << 16 | in(({
            offset_var += 1; offset_var - 1
          })) << 24
        }
        ({
          i += 1; i - 1
        })
      }
    }
    A = ({
      Ap = h0; Ap
    })
    B = ({
      Bp = h1; Bp
    })
    C = ({
      Cp = h2; Cp
    })
    D = ({
      Dp = h3; Dp
    })
    E = ({
      Ep = h4; Ep
    });
    {
      i = 0
      while (i < 16) {
        {
          s = RipeMD160.S(i)
          T = A + (B ^ C ^ D) + X(i)
          A = E
          E = D
          D = C << 10 | C >>> 22
          C = B
          B = (T << s | T >>> (32 - s)) + A
          s = RipeMD160.Sp(i)
          T = Ap + (Bp ^ (Cp | ~Dp)) + X(RipeMD160.Rp(i)) + 0x50A28BE6
          Ap = Ep
          Ep = Dp
          Dp = Cp << 10 | Cp >>> 22
          Cp = Bp
          Bp = (T << s | T >>> (32 - s)) + Ap
        }
        ({
          i += 1; i - 1
        })
      }
    }
    while (i < 32) {
      {
        s = RipeMD160.S(i)
        T = A + ((B & C) | (~B & D)) + X(RipeMD160.R(i)) + 0x5A827999
        A = E
        E = D
        D = C << 10 | C >>> 22
        C = B
        B = (T << s | T >>> (32 - s)) + A
        s = RipeMD160.Sp(i)
        T = Ap + ((Bp & Dp) | (Cp & ~Dp)) + X(RipeMD160.Rp(i)) + 0x5C4DD124
        Ap = Ep
        Ep = Dp
        Dp = Cp << 10 | Cp >>> 22
        Cp = Bp
        Bp = (T << s | T >>> (32 - s)) + Ap
      }
      ({
        i += 1; i - 1
      })
    }
    while (i < 48) {
      {
        s = RipeMD160.S(i)
        T = A + ((B | ~C) ^ D) + X(RipeMD160.R(i)) + 0x6ED9EBA1
        A = E
        E = D
        D = C << 10 | C >>> 22
        C = B
        B = (T << s | T >>> (32 - s)) + A
        s = RipeMD160.Sp(i)
        T = Ap + ((Bp | ~Cp) ^ Dp) + X(RipeMD160.Rp(i)) + 0x6D703EF3
        Ap = Ep
        Ep = Dp
        Dp = Cp << 10 | Cp >>> 22
        Cp = Bp
        Bp = (T << s | T >>> (32 - s)) + Ap
      }
      ({
        i += 1; i - 1
      })
    }
    while (i < 64) {
      {
        s = RipeMD160.S(i)
        T = A + ((B & D) | (C & ~D)) + X(RipeMD160.R(i)) + 0x8F1BBCDC
        A = E
        E = D
        D = C << 10 | C >>> 22
        C = B
        B = (T << s | T >>> (32 - s)) + A
        s = RipeMD160.Sp(i)
        T = Ap + ((Bp & Cp) | (~Bp & Dp)) + X(RipeMD160.Rp(i)) + 0x7A6D76E9
        Ap = Ep
        Ep = Dp
        Dp = Cp << 10 | Cp >>> 22
        Cp = Bp
        Bp = (T << s | T >>> (32 - s)) + Ap
      }
      ({
        i += 1; i - 1
      })
    }
    while (i < 80) {
      {
        s = RipeMD160.S(i)
        T = A + (B ^ (C | ~D)) + X(RipeMD160.R(i)) + 0xA953FD4E
        A = E
        E = D
        D = C << 10 | C >>> 22
        C = B
        B = (T << s | T >>> (32 - s)) + A
        s = RipeMD160.Sp(i)
        T = Ap + (Bp ^ Cp ^ Dp) + X(RipeMD160.Rp(i))
        Ap = Ep
        Ep = Dp
        Dp = Cp << 10 | Cp >>> 22
        Cp = Bp
        Bp = (T << s | T >>> (32 - s)) + Ap
      }
      ({
        i += 1; i - 1
      })
    }
    T = h1 + C + Dp
    h1 = h2 + D + Ep
    h2 = h3 + E + Ap
    h3 = h4 + A + Bp
    h4 = h0 + B + Cp
    h0 = T
  }

  protected def padBuffer: Array[Byte] = {
    val n: Int = (count % RipeMD160.BLOCK_SIZE).asInstanceOf[Int]
    var padding: Int = if ((n < 56)) (56 - n) else (120 - n)
    val result: Array[Byte] = new Array[Byte](padding + 8)
    result(0) = 0x80.toByte
    val bits: Long = count << 3
    result(({
      padding += 1; padding - 1
    })) = bits.toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 8).toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 16).toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 24).toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 32).toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 40).toByte
    result(({
      padding += 1; padding - 1
    })) = (bits >>> 48).toByte
    result(padding) = (bits >>> 56).toByte
    return result
  }

  protected def getResult: Array[Byte] = {
    val result: Array[Byte] = Array[Byte](h0.toByte, (h0 >>> 8).toByte, (h0 >>> 16).toByte, (h0 >>> 24).toByte, h1.toByte, (h1 >>> 8).toByte, (h1 >>> 16).toByte, (h1 >>> 24).toByte, h2.toByte, (h2 >>> 8).toByte, (h2 >>> 16).toByte, (h2 >>> 24).toByte, h3.toByte, (h3 >>> 8).toByte, (h3 >>> 16).toByte, (h3 >>> 24).toByte, h4.toByte, (h4 >>> 8).toByte, (h4 >>> 16).toByte, (h4 >>> 24).toByte)
    return result
  }

  protected def resetContext {
    h0 = 0x67452301
    h1 = 0xEFCDAB89
    h2 = 0x98BADCFE
    h3 = 0x10325476
    h4 = 0xC3D2E1F0
  }
}
