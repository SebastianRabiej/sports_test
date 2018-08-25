import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.json.JSON
import org.tools4j.groovytables.GroovyTables
import org.tools4j.groovytables.Rows
import spock.lang.Shared
import spock.lang.Specification


class MatchInformationSpec extends Specification {
    final int NUMBER_OF_SCORE_ROW = 0

    @Shared RESTClient restClient

    def setupSpec() {
        restClient = new RESTClient("http://46.101.141.19:8000", JSON)
        restClient.handler.failure = { resp -> resp } //FIX niedoskonałości RestClienta.
    }

    def "Zmiana wyniku meczu poprzez dodanie gola"(){
        given:"Zakładając, że dane początkowe dla meczu to"
        mockMatch{
            fc_barcelona   | real_madryt
                  0        |      0
        }

        when:"Gdy dodaje Gol dla FC Barcelony"
        restClient.post(path: "/addGoal", body: "fc_barcelona", contentType: ContentType.JSON)


        then:"Wtedy dane wyglądają następująco:"
        assertMatchInformation{
            fc_barcelona   | real_madryt
                  1        |      0
        }
    }

    def "Zmiana wyniku meczu po pomylce"(){
        given:"Zakładając, że dane początkowe dla meczu to"
        mockMatch{
            fc_barcelona   | real_madryt
                  1        |      0
        }

        when:"Gdy dodaje cofam gol dla FC Barcelony"
        restClient.post(path: "/reverseGoal", body: "fc_barcelona", contentType: ContentType.JSON)


        then:"Wtedy dane wyglądają następująco:"
        assertMatchInformation{
            fc_barcelona   | real_madryt
                  0        |      0
        }
    }

    def "Karny, po którym strzelany jest gol"(){
        given:"Zakładając, że dane początkowe dla meczu to"
        mockMatch{
            fc_barcelona   | real_madryt
                  1        |      0
        }
        and: "I początkowa liczba karnych to"
        mockPenalties{
            fc_barcelona   | real_madryt
                  0        |      0
        }

        when:"Gdy dodaje informację o karnym dla Real Madryt"
        restClient.post(path: "/addPenalty", body: "real_madryt", contentType: ContentType.JSON)

        and:"po którym strzela się gola"
        restClient.post(path: "/addGoal", body: "real_madryt", contentType: ContentType.JSON)

        then:"Wtedy dane wyglądają następująco:"
        assertMatchInformation{
            fc_barcelona   | real_madryt
                  1        |      1
        }

        assertPenaltiesInformation{
            fc_barcelona   | real_madryt
                  0        |      1
        }
    }

    void mockMatch(Closure closure) {
        MatchInfo matchInfo = createMatchInfo(closure)
        restClient.post(path: "/mockMatch", body: matchInfo, contentType: ContentType.JSON)
    }

    void mockPenalties(Closure closure) {
        PenaltiesInfo penaltiesInfo = createPenaltiesInfo(closure)
        restClient.post(path: "/mockPenalties", body: penaltiesInfo, contentType: ContentType.JSON)
    }

    PenaltiesInfo createPenaltiesInfo(Closure closure) {
        Rows rows = GroovyTables.createRows(closure)
        return new PenaltiesInfo(rows.getColumnHeadings().get(0),
                                 rows.get(NUMBER_OF_SCORE_ROW).getValues().get(0),
                                 rows.getColumnHeadings().get(1),
                                 rows.get(NUMBER_OF_SCORE_ROW).getValues().get(1))
    }

    private MatchInfo createMatchInfo(Closure closure) {
        Rows rows = GroovyTables.createRows(closure)
        Team host = new Team(rows.getColumnHeadings().get(0),
                             rows.get(NUMBER_OF_SCORE_ROW).getValues().get(0))
        Team quest = new Team(rows.getColumnHeadings().get(1),
                              rows.get(NUMBER_OF_SCORE_ROW).getValues().get(1))
        new MatchInfo(host: host, guest: quest)
    }

    void assertMatchInformation(Closure closure) {
        HttpResponseDecorator resp = restClient.get(path: "/matchInfo", contentType: ContentType.JSON)
        MatchInfo resultMatchInfo = new MatchInfo(resp.responseData)
        MatchInfo assumedMatchInfo = createMatchInfo(closure)

        assert resultMatchInfo.host.name == assumedMatchInfo.host.name
        assert resultMatchInfo.guest.name == assumedMatchInfo.guest.name
        assert resultMatchInfo.host.name == assumedMatchInfo.host.name
        assert resultMatchInfo.guest.score == assumedMatchInfo.guest.score
    }

    void assertPenaltiesInformation(Closure closure) {
        HttpResponseDecorator resp = restClient.get(path: "/penaltiesInfo", contentType: ContentType.JSON)
        PenaltiesInfo resultPenaltiesInfo = new PenaltiesInfo(resp.responseData)
        PenaltiesInfo assumedMatchInfo = createPenaltiesInfo(closure)

        assert resultPenaltiesInfo.hostTeamName == assumedMatchInfo.hostTeamName
        assert resultPenaltiesInfo.hostNumberOfPenalties == assumedMatchInfo.hostNumberOfPenalties
        assert resultPenaltiesInfo.guestTeamName == assumedMatchInfo.guestTeamName
        assert resultPenaltiesInfo.guestNumberOfPenalties == assumedMatchInfo.guestNumberOfPenalties
    }
}