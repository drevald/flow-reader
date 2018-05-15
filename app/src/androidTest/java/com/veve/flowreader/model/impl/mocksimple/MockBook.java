package com.veve.flowreader.model.impl.mocksimple;

import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;

/**
 * Created by ddreval on 12.01.2018.
 */

public class MockBook implements Book {

    private int currentPage = 0;

//    private static final String[] PAGES = {
//
//            "PROLOG\n" +
//                    "HUR DE RAKADE HADE DET 1 SKÅNE I KVNG HARALD BLÅTANDS TID\n" +
//                    "✓ MÅNGA oroliga män foro bort från Skåne med Bue och Vagn och hade ingen lycka i Hjörungavåg; andra följde Styrbjörn till Uppsala och föllo med honom. När det spordes hemma att få voro att vänta tillbaka, blevo sorge-kväden framsagda och minnesstenar resta, varpå allt förståndigt folk var överens att det var bäst som skett, i det man nu kunde hoppas på mera stillhet än förr och färre ägoskiften med eggjärn. Det blev nu ymniga år, både med råg och sill, och de flesta trivdes gott; men de som tyckte att grödorna kommo långsamt foro till England och Irland och hade god härnad, och många stannade därute.\n" +
//                    "Nu hade rakade män börjat komma till Skåne, både från saxarnas land och från England, för att predika den kristna läran. De hade mycket att ta.a om, och folk var först nyfiket och lyssnade gärna; och kvinnor funno det nöjsamt att ........... av främlingarna och få en vit sä.k till skänks. Men snart hade främlingarna ont om särkar; och\n"
//            ,
//
//            "folk upphörde att lyssna till deras predikningar, som fo-reföllo tröttsamma och föga trovärdiga; dessutom talade  de ett hackigt tungomål, som de lärt sig i Hedeby eller,  på de västra öarna, och tedde sig därför barnsliga till förståndet.\n" +
//                    "Det gick därför smått med kristnandet; och de rakade, som talade mycket om frid och som över allt annat voro upptända av våldsamhet mot gudarna, grepos stundom av religiösa mån och hängdes upp i heliga askträd och fingo pilar i sig och gåvos åt Odins fåglar. Men andra, som nått norrut till göingarnas skogar, där föga religion fanns, hälsades med glädje och leddes bundna till marknader i Småland och byttes bort mot oxar och bäverskinn. Som trälar hos smålänningarna läto några håret växa och kände sig missnöjda med Jehovah och gjorde gott skäl för sig; men de flesta fortsatte att vilja störta gudarna och doppa kvinnor och barn, hellre än att bryta sten och mala korn, och vållade sina husbönder så stor förtret, att göingama snart inte kunde få ett par treårs smålandsoxar för en fullgod präst utan en mellangift av salt eller vadmal. Då rådde dåligt humör mot de rakade i gränstrakterna.\n" +
//                    "En sommar hade bud gått runt hela Danavälde att kung Harald Blåtand anammat den nya läran. I sina unga dagar hade han gjort ett försök och hastigt ångrat sig; men nu var han på allvar gången över. Ty kung Harald var nu gammal och hade länge plågats av svår värk i sin rygg, så att han haft ringa glädje av sitt öl och sina kvinnor; och kloka biskopar, som kejsaren sänt, hade nu gnidit honom med björnister, som stärkts med apostlanamn, och svept honom i fårskinn och givit honom signat ört-vatten i stället för öl och tecknat kors mellan hans axlar och läst många djävlar ur honom, tills värken gått bort och kungen blivit kristen."
//            ,
//
//            "Gudsmannen hade därvid lovat att värre elände skulle drabba honom, om han Ster hemfölle at blot eller visade sig ljum i tron. Därför befallde kung Harald, sedan ban blivit rörlig igen och kunnat taga till sig en ung morisk slavinna, som Olof med Ädelstenarna, konung av Cork, sänt honom som vängåva, att allt folket skulle låta sig kristnas; och ehuru sidan t tal tycktes sällsamt från den som själv härstammade från Odin, lydde många hans pi-bud, ty han hade styrt länge och med lycka och hade därför mycket att säga i landet. Han lade de hirdaste straff på dem som buro hand på präster; och i Skåne tilltogo nu dessa i antal, och kyrkor byggdes på slätten; och de gamla gudarna började komma ur bruk utom i sjönöd och vid kreaturssjukdom.\n\n" +
//                    "Men i Göingc skrattades mycket it allt detta. Ty folket i gränsskogarna hade lättare för att skratta än det förståndiga folket på leran, och åt kungars befallningar skrattade de mest. I de trakterna nådde fi mäns makt längre än deras högra arm, och från Jellinge till Göingc var lång väg Sven för de största kungar. I gamla dagar, pi Harald Hildetands och Ivar Vidfamnes tid och dessförinnan, hade kungar brukat komma till Göinge för att jaga vildoxar i de stora skogarna, men sillan i andra ärenden. Sedan dess hade vildoxarna tagit slut, och kungars besök med dem; och om nu någon kung retade upp sig över ohörsamhet eller mager skatt och hotade att komma dit, brukade han fl det svaret, att inga vildoxar synts till i trakterna, men att man skulle underrätta om så skedde, och taga vanligt emot honom då. Därför var det sedan linge ett stäv hos gränsborna, att bland dem skulle ingen kung komma förrän vildoxarna kommo tillbaka.\n\n" +
//                    "Så förblev allt som det varit i Göinge, och ingen kristendom kom i gang där. De präster som försökte sig dit\n",
//
//            "såldes fortfarande över gränsen; men somliga göingar tyckte att man rätteligen borde slå ihjäl dem på fläcken och börja krig mot det näriga folket i Sunnerbo och Allbo, emedan smålänningarnas pris inte gav skälig förtjänst på bandeln.",
//
//            "Första avdelningen\n\nDEN LÄNGA RESAN",
//
//            "Första kapitlet\n" +
//                    "OM BONDEN TOSTE OCH HANS HUSHALL\n" +
//                    "Vid kusten bodde folket i byar, för näringens skull och till ökad trygghet; ty strandhugg försöktes ofta frän skepp som rundade Skåne, både på våren, av man på utfärd som önskade förse sig med billig färskmat, och på hösten, av dem som tomhänta voro på hemfärd från felslagen här-nad. Horn blåstes i natten, när landstigna flockar för-nummos, och kallade grannar till hjälp; och hemmafolk i en god by kunde stundom själv taga ett skepp eller två, från främlingar som voro oförsiktiga, och ha vackert byte att visa byns utfarare när långskeppen kommo hem till vintervila.\n" +
//                    "Men rika och stolta män, som hade eget skepp, funno det svårt att ha grannar nära och bodde helst var för sig; ty även när de lågo på sjön, höllo de sina gårdar försvarade med goda män som sutto hemma. I Kullabygden fun-nos många sådana stormän; där hade de rika bönderna rykte om sig att vara högmodigare än på andra håll När de voro hemma, kivades de garna med varandra, fast det fanns gott utrymme mellan gårdarna; men de voro ofta borta, ty från barndomen sågo de ut över havet och höllo det för sin egen utmark, där alla som mötte dem finge skylla sig själva.\n"
//    };

//    private static final String[] PAGES = {
//            "OIUOIUOJLJKLJLKJLKJLKJLKJAKLJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJKL",
//            "OIUOIUOJLJKLJLKJLKJLKJLKJAKLJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJKL",
//            "OIUOIUOJLJKLJLKJLKJLKJLKJAKLJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJKL",
//            "OIUOIUOJLJKLJLKJLKJLKJLKJAKLJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJKL",
//            "OIUOIUOJLJKLJLKJLKJLKJLKJAKLJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJLKJKL"
//    };


        private static final String[] PAGES = {"ЮЮЮ"};


    @Override
    public void setCurrentPageNumber(int pageNumber) {
        currentPage = pageNumber;
    }

    @Override
    public int getCurrentPageNumber() {
        return currentPage;
    }

    @Override
    public BookPage getPage(int pageNumber) {
        return new MockPageImpl(PAGES[pageNumber]);
    }

    @Override
    public int getPagesCount() {
        return PAGES.length;
    }

    @Override
    public String getName() {
        return "Sample DjVu Book";
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getPath() {
        return null;
    }
}
