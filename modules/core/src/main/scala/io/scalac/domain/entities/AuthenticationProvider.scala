package io.scalac.domain.entities

import com.byteslounge.slickrepo.meta.{Versioned, VersionedEntity}
import com.byteslounge.slickrepo.repository.VersionedRepository
import org.joda.time.DateTime
import slick.ast.BaseTypedType
import slick.basic.DatabaseConfig

import io.scalac.common.core.AuthenticationProviderId
import io.scalac.common.db.PostgresJdbcProfile

/**
  * Represents a linked login for an identity (i.e. a local username/password or a Facebook/Google account).
  *
  * The login info contains the data about the provider that authenticated that identity.
  *
  * @param providerId The ID of the provider.
  * @param providerKey A unique key which identifies a user on this provider (userID, email, ...).
  */
final case class AuthenticationProvider(
  override val id: Option[AuthenticationProviderId],
  providerId: String,
  providerKey: String,//email in our case
  createdAt: DateTime,
  updatedAt: DateTime,
  override val version: Option[Int]
) extends VersionedEntity[AuthenticationProvider, AuthenticationProviderId, Int] {

  override def withId(id: AuthenticationProviderId): AuthenticationProvider = this.copy(id = Some(id))
  override def withVersion(version: Int): AuthenticationProvider = this.copy(version = Some(version))
}

class AuthenticationProvidersSlickPostgresRepository(
  val dbConfig: DatabaseConfig[PostgresJdbcProfile]
) extends VersionedRepository[AuthenticationProvider, AuthenticationProviderId, Int](dbConfig.profile)
    with CommonMappers {

  import dbConfig.profile.api._
  override type TableType = AuthenticationProvidersTable
  override val versionType = implicitly[BaseTypedType[Int]]
  override def pkType = implicitly[BaseTypedType[AuthenticationProviderId]]
  override def tableQuery = TableQuery[AuthenticationProvidersTable]

  class AuthenticationProvidersTable(tag: slick.lifted.Tag)
    extends Table[AuthenticationProvider](tag, "authentication_providers") with Versioned[AuthenticationProviderId, Int] {

    override def id = column[AuthenticationProviderId]("id", O.PrimaryKey)
    def providerId = column[String]("provider_id", O.SqlType("VARCHAR(50)"))
    def providerKey = column[String]("provider_key", O.SqlType("VARCHAR(200)"))
    def createdAt = column[DateTime]("created_at", O.SqlType("timestamptz"))
    def updatedAt = column[DateTime]("updated_at", O.SqlType("timestamptz"))
    override def version = column[Int]("version")

    def * = (id.?, providerId, providerKey, createdAt, updatedAt, version.?) <> ((
      AuthenticationProvider.apply _).tupled, AuthenticationProvider.unapply)
  }

  val findByProviderIdAndKey = Compiled { (providerId: Rep[String], providerKey: Rep[String]) =>
    tableQuery.filter(t => t.providerId === providerId && t.providerKey === providerKey)
  }
}
