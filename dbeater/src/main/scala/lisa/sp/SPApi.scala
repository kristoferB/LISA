package lisa.sp

import org.joda.time.DateTime
import lisa.endpoint.message._


// Input
class getSP
case class getRawProductEvents(search: SearchSP) extends getSP
case class getRawMachineEvents(search: SearchSP) extends getSP
case object getFakeMachineEvents extends getSP

case class getFilledProductEvents(search: SearchSP) extends getSP
case class getFilledMachinesEvents(search: SearchSP) extends getSP

case class getProductFolds(search: SearchSP) extends getSP
case class getMachineFolds(search: SearchSP) extends getSP
case class getPositionFolds(search: SearchSP) extends getSP

case class GetFromTopic(topic: String, search: SearchSP) extends getSP
case class GetFromRawTopic(topic: String, search: SearchSP) extends getSP

case class GetLeadTimeChart(topic: String, search: SearchSP) extends getSP
case class GetLeadTimeDistrChart(topic: String, search: SearchSP) extends getSP
case class GetProductPosChart(topic: String, pid: String, search: SearchSP) extends getSP
case class GetPositionChart(topic: String, search: SearchSP) extends getSP
case class GetResourceUtilChart(topic: String, search: SearchSP) extends getSP
case object GetResourceState

case class AddResourceToUtilChart(machine: String) extends getSP


case class SearchSP(filter: Option[FilterSP],
                    pagination: Option[PaginationSP],
                    sort: Option[SortSP])


case class PaginationSP(start: Int, no: Int)
case class FilterSP(kv: Map[String, LISAValue],
                    startDate: Option[LISAValue] = None,
                    stopDate: Option[LISAValue] = None)
case class SortSP(attr: String, des: Boolean);

trait setSP
case class SendEventSP(attributes: Map[String, LISAValue], topic: Option[String]) extends setSP



// Output
case class EventsSP(list: List[Map[String, LISAValue]])

case class Chart(cols: List[Col], rows: List[Row])
case class Col(id: String, label: String, `type`: String)
case class Row(c: List[VF])
case class VF(v: LISAValue, f: Option[String] = None)