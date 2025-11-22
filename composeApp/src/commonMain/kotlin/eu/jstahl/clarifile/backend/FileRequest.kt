package eu.jstahl.clarifile.backend

enum class LogicalOperator {
    Or,
    And
}

data class FileRequest(
    val tags: List<String>,
    val tagOperator: LogicalOperator = LogicalOperator.Or,
    val searchString: String = ""
)
