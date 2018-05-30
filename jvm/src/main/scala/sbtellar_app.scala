/**
 * Created by kring on 2015/7/18.
 */

import java.io.{File, FileOutputStream}
import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.directives.FormFieldDirectives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters

import scala.concurrent._
import scala.concurrent.duration._

import scala.sys.process._

object SbtellarApp extends App {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  val route =
    path("info") {
      (rc: RequestContext) => {
        Http().singleRequest(HttpRequest(uri = Uri("http://localhost:39132/info"))).
          flatMap { (resp) =>
          rc.complete(resp)
        }(rc.executionContext)
      }
    }~
      path("hist" / RemainingPath) { pathRest =>
        get {
          getFromFile(new File("history/" + pathRest))
        } ~
          put {
            extractLog { logger =>
            { (rc:RequestContext) =>
                val file = new File("history/" +pathRest)
                file.getParentFile().mkdirs();
                file.createNewFile();
                val mat = rc.request.entity.dataBytes.runWith(StreamConverters.fromOutputStream(() => new FileOutputStream(file)))
                mat.flatMap( filesize => {
                  //logger.info(s"wrote $pathRest $filesize/${rc.request.entity.getContentLengthOption()} bytes")
                  rc.complete("OK")
                })(rc.executionContext)
            }
            }
          }
      } ~
      path("tx") {
        //support `curl -F blob="BASE64+/ENCODED+/TRANSACTION" http://localhost:8080/tx` without manual urlencoding
        formFields('blob) {
          (blob) =>  {
          (rc: RequestContext) => {
          Http().singleRequest(HttpRequest(uri = Uri("http://localhost:39132/tx").withQuery(("blob", blob) +: Uri.Query.Empty))).
            flatMap { (resp) =>
            rc.complete(resp)
          }(rc.executionContext)
          }
          }
        }
      }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

//  Seq(Paths.get("bin", "stellar-core").toString,"--conf", "bin/stellar-core.cfg", "--newhist", "single") !
//
//  Seq(Paths.get("bin", "stellar-core").toString,"--conf", "bin/stellar-core.cfg", "--newdb", "--forcescp") !
//
//  val core = Seq(Paths.get("bin", "stellar-core").toString,"--conf", "bin/stellar-core.cfg").run()

  //for scala 2.11
  //scala.io.StdIn.readLine(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  //for scala 2.10
  scala.Console.readLine(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  import system.dispatcher // for the future transformations
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => {
//    core.destroy()
    Await.result(system.terminate(), 5 seconds)
  }) // and shutdown when done
}
