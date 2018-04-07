package models.digital_ocean

import java.time.ZonedDateTime

case class Component(name: String,
                     status: String,
                     created_at: ZonedDateTime,
                     updated_at: ZonedDateTime,
                     position: Int,
                     description: Option[String],
                     showcase: Boolean,
                     id: String,
                     page_id: String,
                     group_id: Option[String]
                    )
