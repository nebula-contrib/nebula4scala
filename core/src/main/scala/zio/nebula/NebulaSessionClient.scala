package zio.nebula

import scala.jdk.CollectionConverters._

import zio._

import com.vesoft.nebula.client.graph._
import com.vesoft.nebula.client.graph.data.HostAddress

/**
 * @author
 *   梦境迷离
 * @version 1.0,2023/8/29
 */
trait NebulaSessionClient {

  /**
   * init the SessionPool
   */
  def init(): Task[Boolean]

  /**
   * Execute the nGql sentence.
   * @param stmt
   *   The nGql sentence. such as insert ngql `INSERT VERTEX person(name) VALUES "Tom":("Tom");`
   * @return
   */
  def execute(stmt: String): Task[NebulaResultSet]

  /**
   * close the session pool
   */
  def close(): Task[Unit]

  /**
   * if the SessionPool has been initialized
   */
  def isActive: Task[Boolean]

  /**
   * if the SessionPool is closed
   */
  def isClosed: Task[Boolean]

  /**
   * get the number of all Session
   */
  def sessionNum: Task[Int]

  /**
   * get the number of idle Session
   */
  def idleSessionNum: Task[Int]
}

object NebulaSessionClient {

  lazy val layer: ZLayer[Scope & NebulaSessionPoolConfig, Throwable, NebulaSessionClient] = ZLayer.fromZIO {
    for {
      nebulaConfig <- ZIO.service[NebulaSessionPoolConfig]
      sessionPool  <- ZIO.acquireRelease(
                        ZIO.attempt(
                          new SessionPool(
                            new SessionPoolConfig(
                              nebulaConfig.address.map(d => new HostAddress(d.host, d.port)).asJava,
                              nebulaConfig.spaceName,
                              nebulaConfig.auth.username,
                              nebulaConfig.auth.password
                            ).setMaxSessionSize(nebulaConfig.maxSessionSize)
                              .setMinSessionSize(nebulaConfig.minSessionSize)
                              .setRetryTimes(nebulaConfig.retryTimes)
                              .setWaitTime(nebulaConfig.waitTimeMills)
                              .setIntervalTime(nebulaConfig.intervalTimeMills)
                              .setTimeout(nebulaConfig.timeoutMills)
                              .setCleanTime(nebulaConfig.cleanTimeSeconds)
                              .setReconnect(nebulaConfig.reconnect)
                              .setHealthCheckTime(nebulaConfig.healthCheckTimeSeconds)
                              .setUseHttp2(nebulaConfig.useHttp2)
                          )
                        )
                      )(release => ZIO.attempt(release.close()).ignoreLogged)
    } yield new NebulaSessionClientLive(sessionPool)
  }
}
