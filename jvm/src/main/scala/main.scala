import org.codeck.sbtripple.AccountFamily


import org.bouncycastle.jce.provider.BouncyCastleProvider
import javax.net.ssl.{SSLContext, SSLParameters, TrustManager, X509TrustManager}
import java.security.cert.X509Certificate
import java.security.SecureRandom

import akka.stream.io.{SslTls, NegotiateNewSession,SslTlsOutbound, SslTlsInbound, SessionBytes, SendBytes, Role, Closing}
import akka.stream.scaladsl.{Source, Flow, Sink, Tcp=>StreamTcp}
import StreamTcp.{ServerBinding, IncomingConnection}
import akka.stream.stage.{StatefulStage, PushStage, Context, Directive}
import scala.concurrent._
import scala.concurrent.duration._

import akka.util.ByteString

object JSSEHelper {
  val sc = SSLContext.getInstance("TLSv1.2")

  val AllTrusting = Array[TrustManager](
    new X509TrustManager {
      def getAcceptedIssuers(): Array[X509Certificate] = new Array[X509Certificate](0)
      def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
      def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
    })
  sc.init(null, AllTrusting, new SecureRandom())

  val AllCipherSuites = collection.immutable.Seq(sc.getSupportedSSLParameters.getCipherSuites:_*)
}

object MainApp extends App {

  val bcp = new BouncyCastleProvider()
  java.security.Security.addProvider(bcp)

  //System.setProperty("javax.net.debug", "all")
  println("go");

  implicit val sys = akka.actor.ActorSystem();
  import sys.dispatcher
  implicit val mat = akka.stream.ActorMaterializer

  val conn = StreamTcp().outgoingConnection("127.0.0.1", 4433)
  //val conn = StreamTcp().outgoingConnection("validator-03.stellar.org", 52001)
  //val conn = StreamTcp().outgoingConnection("192.170.145.70", 51235)

  val tls = SslTls(JSSEHelper.sc, new NegotiateNewSession(None, None, None, None), Role.client)
  val tslconn = tls.join(conn)
  //val starter = Source.single[SslTlsOutbound]();
//  val f = tslconn.join(Flow[SslTlsInbound].map({
//    case SessionBytes(session, bytes) =>
//      println(session, bytes)
//      SendBytes(bytes)
//      //SendBytes(ByteString())
//    case s@_ =>
//      println(s)
//      SendBytes(ByteString())
//  } ))
//
//  f.run()

  //for scala 2.11
  //  scala.io.StdIn.readLine("Press Enter to Exit: ")
  //for scala 2.10
  scala.Console.readLine("Press Enter to Exit: ")

  sys.shutdown
  println("Done")
}

