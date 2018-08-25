import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.json.JSON
import org.tools4j.groovytables.GroovyTables
import spock.lang.Shared
import spock.lang.Specification

class RankingSpec extends Specification {
    final int NUMBER_OF_SCORE_ROW = 0

    @Shared RESTClient restClient

    def setupSpec() {
        restClient = new RESTClient("http://46.101.141.19:8000", JSON)
        restClient.handler.failure = { resp -> resp } //FIX niedoskonałości RestClienta.
    }

    def "Zmiana w tabeli rankingowej gdy jest więcej punktów"(){
        given:"Zakładając, że początkowa klasyfikacja zespołów to:"
        mockRanking{
            idDruzyny | nazwaDrużyny  | RozegraneMecze | Bramki | Punkty
            1	      | "Fc Barcelona"|     2          |   5    |    6
            2         | "Real Madryt" |     2          |   3    |    6
        }

        when:"Gdy dodajemy informację, o skończonym meczu"
        MatchInfoForRanking matchInfo = createMatchInfoForRanking {
            idDruzyny | gole
            1         | 0
            2         | 3
        }
        restClient.post(path: "/addMatchInformation", body: matchInfo, contentType: ContentType.JSON)


        then:"Wtedy klasyfikacja zespołów wyglądaja następująco:"
        assertRanking{
            idDruzyny | nazwaDrużyny  | RozegraneMecze | Bramki | Punkty
            2	      | "Real Madryt" |     3          |   6    |    9
            1         | "Fc Barcelona"|     3          |   5    |    6
        }
    }

    def "Zmiana w tabeli rankingowej gdy jest więcej goli"(){
        given:"Zakładając, że początkowa klasyfikacja zespołów to:"
        mockRanking{
            idDruzyny | nazwaDrużyny  | RozegraneMecze | Bramki | Punkty
            1	      | "Fc Barcelona"|     2          |   5    |    9
            2         | "Real Madryt" |     2          |   3    |    6
        }

        when:"Gdy dodajemy informację, o skończonym meczu"
        MatchInfoForRanking matchInfo = createMatchInfoForRanking {
            idDruzyny | gole
            1         | 0
            2         | 3
        }
        restClient.post(path: "/addMatchInformation", body: matchInfo, contentType: ContentType.JSON)


        then:"Wtedy klasyfikacja zespołów wyglądaja następująco:"
        assertRanking{
            idDruzyny | nazwaDrużyny  | RozegraneMecze | Bramki | Punkty
            2	      | "Real Madryt" |     3          |   6    |    9
            1         | "Fc Barcelona"|     3          |   5    |    9
        }
    }

    def mockRanking(Closure closure) {
        Ranking ranking = createRanking(closure)
        restClient.post(path: "/mockRanking", body: ranking, contentType: ContentType.JSON)
    }

    Ranking createRanking(Closure closure) {
        List<RankingRow> rows = GroovyTables.createListOf(RankingRow.class).fromTable(closure)
        new Ranking(rows)
    }

    MatchInfoForRanking createMatchInfoForRanking(Closure closure) {
        List<TeamMatchInfo> teamsInfo = GroovyTables.createListOf(TeamMatchInfo.class).fromTable(closure)
        new MatchInfoForRanking(teamsInfo.get(0), teamsInfo.get(1))
    }

    void assertRanking(Closure closure) {
        HttpResponseDecorator resp = restClient.get(path: "/rankingInfo", contentType: ContentType.JSON)
        Ranking resultRanking = new Ranking(resp.responseData)
        Ranking assumedRanking = createRanking(closure)

        assert resultRanking.rows.get(0).idDruzyny ==  assumedRanking.rows.get(0).idDruzyny
        assert resultRanking.rows.get(0).nazwaDruzyny ==  assumedRanking.rows.get(0).nazwaDruzyny
        assert resultRanking.rows.get(0).rozegraneMecze ==  assumedRanking.rows.get(0).rozegraneMecze
        assert resultRanking.rows.get(0).bramki ==  assumedRanking.rows.get(0).bramki
        assert resultRanking.rows.get(0).punkty ==  assumedRanking.rows.get(0).punkty

        assert resultRanking.rows.get(1).idDruzyny ==  assumedRanking.rows.get(1).idDruzyny
        assert resultRanking.rows.get(1).nazwaDruzyny ==  assumedRanking.rows.get(1).nazwaDruzyny
        assert resultRanking.rows.get(1).rozegraneMecze ==  assumedRanking.rows.get(1).rozegraneMecze
        assert resultRanking.rows.get(1).bramki ==  assumedRanking.rows.get(1).bramki
        assert resultRanking.rows.get(1).punkty ==  assumedRanking.rows.get(1).punkty
    }

}