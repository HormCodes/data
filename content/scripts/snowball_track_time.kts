#!/bin/bash



//usr/bin/env echo '
/**** BOOTSTRAP kscript ****\'>/dev/null
command -v kscript >/dev/null 2>&1 || curl -L "https://git.io/fpF1K" | bash 1>&2
exec kscript $0 "$@"
\*** IMPORTANT: Any code including imports and annotations must come after this line ***/


//DEPS khttp:khttp:1.0.0


data class Project(val id: String, val name: String)

val API_URL = "https://api.clickup.com/api/v2"
val HELP_NOTE = "Run script in following way: kscript track_time.kts PERSONAL_CLICKUP_TOKEN"
val PROJECTS = listOf(
        Project("20fjmw", "D4"),
        Project("20hrxh", "Okay"),
        Project("20hrz4", "Snowball")
)


fun getUrlForTimeTracking(taskId: String): String {
    return "$API_URL/task/$taskId/time"
}

fun toMilliseconds(hours: Double): Long {
    return (hours * 3600000).toLong()
}

fun trackTime(taskId: String, timestamp: Long, durationInMs: Long, personalAccessToken: String): Boolean {
    val response = khttp.post(
            url = getUrlForTimeTracking(taskId),
            data = mapOf("time" to durationInMs, "start" to timestamp),
            headers = mapOf("Authorization" to personalAccessToken)
    )
    return response.statusCode == 200
}

fun askForProject(): Project? {
    println("What project?")
    PROJECTS.forEachIndexed { index, project -> println("${index + 1}. ${project.name}") }

    print("Enter project number: ")

    val projectNumberInput = readLine()
    val projectIndex = ((projectNumberInput ?: return null).toInt()) - 1

    if (projectIndex < 0 || projectIndex > PROJECTS.lastIndex) {
        return null
    }

    return PROJECTS[projectIndex]
}

fun askForHours(): Double? {
    print("Enter number of hours: ")
    val timeAmountInput = readLine()
    val hours = (timeAmountInput ?: return null).toDouble()

    return hours
}

fun askForConfirmation(hours: Double, project: Project): Boolean {
    print("$hours hours will be reported to ${project.name}. Is it correct? [y/N]: ")
    val confirmInput = readLine()
    return confirmInput == "y"
}

fun main(args: List<String>): String {
    if (args.isEmpty()) {
        return HELP_NOTE
    }

    val personalAccessToken = args[0]

    if (personalAccessToken == "") {
        return HELP_NOTE
    }

    println("Report time for Snowball (created by Horm)")

    val project = askForProject()
    val hours = askForHours()

    if (project == null || hours == null) {
        return "Incorrect options."
    }

    val confirmation = askForConfirmation(hours, project)

    if (!confirmation) {
        return "Cancelling... Done."
    }

    val milliseconds = toMilliseconds(hours)
    val timestamp = System.currentTimeMillis() - milliseconds

    val result = trackTime(project.id, timestamp, milliseconds, personalAccessToken)

    if (!result) {
        return "Something went wrong..."
    }

    return "Done"
}

println(main(args.toList()))
