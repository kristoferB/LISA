package lisa.endpoint.message

case class LISAMessageHistory(history : List[String]) {
	def ::(s: String) = LISAMessageHistory(s::history)
}
