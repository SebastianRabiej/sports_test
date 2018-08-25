

import groovy.transform.TupleConstructor

@TupleConstructor
class RankingRow {
    Integer idDruzyny;
    String nazwaDruzyny;
    Integer rozegraneMecze;
    Integer bramki;
    Integer punkty;
}
