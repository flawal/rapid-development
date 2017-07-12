package io.scalac.domain.mock

import java.util.UUID

import io.scalac.common.entities.Pagination
import io.scalac.common.services.DatabaseResponse
import io.scalac.domain.dao.MerchantsDao
import io.scalac.domain.entities.Merchant
import io.scalac.domain.services.Criteria

class MerchantsDaoMock extends MerchantsDao {
  override def findByCriteria(criteria: Criteria, pagination: Pagination): DatabaseResponse[Seq[Merchant]] = ???

  override def find(noteId: UUID): DatabaseResponse[Option[Merchant]] = ???

  override def create(note: Merchant): DatabaseResponse[UUID] = ???

  override def update(note: Merchant): DatabaseResponse[Merchant] = ???
}