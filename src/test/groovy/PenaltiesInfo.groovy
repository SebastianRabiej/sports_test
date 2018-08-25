

import groovy.transform.TupleConstructor

@TupleConstructor
class PenaltiesInfo {
    String hostTeamName
    Integer hostNumberOfPenalties
    String guestTeamName
    Integer guestNumberOfPenalties
}
