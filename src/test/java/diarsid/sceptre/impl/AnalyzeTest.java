package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import diarsid.sceptre.api.Analyze;
import diarsid.sceptre.api.model.Output;
import diarsid.sceptre.api.model.Outputs;
import diarsid.support.objects.GuardedPool;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import static org.junit.jupiter.api.Assertions.fail;

import static diarsid.sceptre.api.LogType.BASE;
import static diarsid.sceptre.api.LogType.POSITIONS_CLUSTERS;
import static diarsid.sceptre.api.LogType.POSITIONS_SEARCH;
import static diarsid.support.objects.Pools.pools;
import static diarsid.support.objects.collections.CollectionUtils.nonEmpty;

public class AnalyzeTest {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeTest.class);
    private static Analyze analyzeInstance;
    private static int totalVariantsQuantity;
    private static long start;
    private static long stop;
    
    private Analyze analyze;
    private boolean expectedToFail;
    private String pattern;
    private String noWorseThan;
    private List<String> variants;
    private List<String> expected;
    private Outputs outputs;
    private boolean notExpectedAreCritical;
    
    public AnalyzeTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
        analyzeInstance = Analyze.Builder
                .newInstance()
                .withLogEnabled(true)
//                .withLogSink(new LineByLineLogSink(System.out::println))
                .withLogTypeEnabled(BASE, true)
                .withLogTypeEnabled(POSITIONS_SEARCH, true)
                .withLogTypeEnabled(POSITIONS_CLUSTERS, true)
                .build();

        start = currentTimeMillis();
    }
    
    @AfterAll
    public static void tearDownClass() {
        stop = currentTimeMillis();

        String report = 
                "\n ======================================" +
                "\n ====== Total AnalyzeTest results =====" +
                "\n ======================================" +
                "\n  total time     : %s " + 
                "\n  total variants : %s \n";

        log.info(format(report, stop - start, totalVariantsQuantity));

        Optional<GuardedPool<AnalyzeUnit>> pool = pools().poolOf(AnalyzeUnit.class);
        if ( pool.isPresent() ) {
            GuardedPool<AnalyzeUnit> c = pool.get();
            AnalyzeUnit analyzeData = c.give();
        }
    }
    
    @BeforeEach
    public void setUp() {
        this.analyze = analyzeInstance;
        this.notExpectedAreCritical = true;
    }
    
    @AfterEach
    public void tearDown() {

    }
    
    private void expectedToFail() {
        this.expectedToFail = true;
    }
    
    private void expectedSameOrderAsVariants() {
        if ( nonEmpty(expected) ) {
            throw new IllegalStateException("Expected already set!");
        }
        
        expected = new ArrayList<>(variants);
    }
    
    @Test
    public void test_EnginesCase_engns() {
        pattern = "engns";
        
        variants = asList(
                "Engines",
                "Design");
        
        expected = asList( 
                "Engines");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_EnginesCase_enges() {
        pattern = "enges";
        
        variants = asList(
                "Engines",
                "Design");
        
        expected = asList( 
                "Engines");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_EnginesCase_enins() {
        pattern = "enins";
        
        variants = asList(
                "Engines",
                "Design");
        
        expected = asList( 
                "Engines");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_PhotosCase_phots() {
        pattern = "phots";
        
        variants = asList(
                "Projects",
                "Images/Photos",
                "Photos");
        
        expected = asList( 
                "Photos",
                "Images/Photos"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_EnginesCase_enings() {
        pattern = "enings";
        
        variants = asList(
                "Engines",
                "Design");
        
        expected = asList( 
                "Engines");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_AvailableJavaCase_availabljava () {
        pattern = "avaijava";
        
        variants = asList(
                "Engines/Java/Path/available"
        );
        
        expected = asList( 
                "Engines/Java/Path/available");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_AvailableJavaCase_avaijava () {
        pattern = "avaijava";
        
        variants = asList(
                "Engines/Java/Path/available",
                "Books/Tech/Java/JavaFX"
        );

        expected = asList(
                "Engines/Java/Path/available");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_FilmsListCase_lsftilm() {
        pattern = "lsftilm";
        
        variants = asList(
                "LostFilm",
                "Films/List",
                "Films/List.txt");
        
        expected = asList( 
                "LostFilm",
                "Films/List",
                "Films/List.txt");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_JavaTechCase_jtech() {
        pattern = "jtech";
        
        variants = asList(
                "Books/tech",
                "Tech",
                "Books/Tech/Design",
                "Tech/langs",
                "Books/Tech/Java");
        
        expected = asList( 
                "Books/Tech/Java");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_JavaTechCase_jtec() {
        pattern = "jtec";

        variants = asList(
                "Books/tech",
                "Tech",
                "Books/Tech/Design",
                "Tech/langs",
                "Books/Tech/Java");

        expected = asList(
                "Books/Tech/Java");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_JavaTechCase_tec() {
        pattern = "tec";

        variants = asList(
                "Books/tech",
                "Tech",
                "Books/Tech/Design",
                "Tech/langs",
                "Books/Tech/Java");

        expected = asList(
                "Tech",
                "Books/tech",
                "Books/Tech/Java",
                "Books/Tech/Design",
                "Tech/langs");

        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_techAnotherCase_jtec() {
        pattern = "jtec";
        
        variants = asList(
                "Tech",
                "Tech/langs");
        
        expected = emptyList();
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_techAnotherCase_tec() {
        pattern = "tec";

        variants = asList(
                "Tech",
                "Tech/langs");

        expected = asList(
                "Tech",
                "Tech/langs");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_techAnotherCase2_jtec() {
        pattern = "jtec";
        
        variants = asList(
                "Tech",
                "langs/Tech",
                "Tech/langs");
        
        expected = emptyList();
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_techAnotherCase2_tec() {
        pattern = "tec";

        variants = asList(
                "Tech",
                "langs/Tech",
                "Tech/langs");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_starWarsCase_sarwars() {
        pattern = "sarwars";
        
        variants = asList(
                "Films/Movies/Star.Wars.The.Last.Jedi.2017.D.BDRip.720p.ExKinoRay.mkv"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsBeamCase_beaporg() {
        pattern = "beaporg";
        
        variants = asList(
                "Job/Search/for_sending",
                "Projects/Diarsid/NetBeans/Beam");
        
        expected = asList(
                "Projects/Diarsid/NetBeans/Beam");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_searchSendingBeamCase_beaporg() {
        pattern = "beaporg";
        
        variants = asList(
                "Job/Search/for_sending",
                "Job/Search/Friends");
        
        expected = asList();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_facebookCase_fb() {
        pattern = "fb";
        
        variants = asList(
                "some_Fstring_Bwith_fb_cluster",
                "facebook");
        
        expected = asList(
                "some_Fstring_Bwith_fb_cluster",
                "facebook"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_warCraft3Case_wc3() {
        pattern = "wc3";
        
        variants = asList("WarCraft_3");
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_activeMQCase_mq() {
        pattern = "mq";
        
        variants = asList("D:/DEV/3__Tools/Servers/Messaging_Servers/ActiveMQ/5.15.8");
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_MySQLCase_msql() {
        pattern = "msql";
        
        variants = asList(
                "Tools/Servers/Data_Servers/MySQL",
                "Tech/langs/sql",
                "dev/sql_developer"
        );
        
        expected = asList("Tools/Servers/Data_Servers/MySQL");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_MavenLocalRepo_mvrep() {
        pattern = "mvrep";
        
        variants = asList(
                "Dev/Lib/Maven_Local_Repo",
                "Dev/Lib/Maven_Local_Repo/org",
                "Dev/Lib/Maven_Local_Repo/diarsid"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_gmailWithOtherMLClusterCase_ml() {
        pattern = "ml";
        
        variants = asList(
                "ml",
                "some_ml_string",
                "some_ml",
                "some_Mstring_Lwith_ml_cluster",
                "mail",
                "gmail");
        
        expected = asList(
                "ml",
                "some_ml",
                "some_ml_string",
                "some_Mstring_Lwith_ml_cluster",
                "mail"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_googleWithOtherGLClusterCase_gl() {
        pattern = "gl";
        
        variants = asList(
                "google",
                "english"
        );
        
        expected = asList(
                "google",
                "english"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_gmailWithOtherMLstringCase_ml() {
        pattern = "ml";
        
        variants = asList(
                "some_stMring_wLith_cluster",
                "gmail");
        
        expected = asList();
        
        weightVariantsAndCheckMatching();
    }

    @Disabled("don't know how to discern - ...JV is legal good cluster for .../JVisualvm")
    @Test
    public void test_javaEngines_enginsjv() {
        pattern = "enginsjv";
        
        variants = asList(
                "Engines/Java/Path/JAVA_HOME/bin/jvisualvm.exe",
                "Engines/Java");
        
        expected = asList(
                "Engines/Java",
                "Engines/Java/Path/JAVA_HOME/bin/jvisualvm.exe"
        );
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_javaEngines_enginsjv_positive() {
        pattern = "enginsjv";

        variants = asList(
                "Engines/Java/Path/JAVA_HOME/bin/javavisualvm.exe",
                "Engines/Java");

        expected = asList(
                "Engines/Java",
                "Engines/Java/Path/JAVA_HOME/bin/javavisualvm.exe"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_javaEngines_enginsjva() {
        pattern = "enginsjva";

        variants = asList(
                "Engines/Java/Path/JAVA_HOME/bin/jvisualvm.exe",
                "Engines/Java");

        expected = asList(
                "Engines/Java",
                "Engines/Java/Path/JAVA_HOME/bin/jvisualvm.exe"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_javaEngines_eningsjva() {
        pattern = "eningsjva";

        variants = asList(
                "Engines/Java/Path/JAVA_HOME/bin/jvisualvm.exe",
                "Engines/Java");

        expected = asList(
                "Engines/Java",
                "Engines/Java/Path/JAVA_HOME/bin/jvisualvm.exe"
        );

        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_messageServers_msser() {
        pattern = "msser";
        
        variants = asList(
                "Tools/Servers/Messaging_Servers",
                "Images/Photos/Miniatures"
                );

        expected = asList(
                "Tools/Servers/Messaging_Servers"
        );
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_messageServers_msser_2() {
        pattern = "msser";

        variants = asList(
                "Tools/Servers",
                "Tools/Servers/Messaging_Servers",
                "Tools/Servers/Web_Servers",
                "Dev/Start_MySQL_server"
        );

        expected = asList(
                "Tools/Servers/Messaging_Servers",
                "Dev/Start_MySQL_server"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_webServers_wsr() {
        pattern = "wsr";
        
        variants = asList(
                "Tools/Servers/Web_Servers"
                );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }      
    
    @Test
    public void test_webServers_wesrrv() {
        pattern = "wesrrv";
        
        variants = asList(
                "Tools/Servers/Web_Servers"
                );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_webServers_wbsrrv() {
        pattern = "wbsrrv";

        variants = asList(
                "Tools/Servers/Web_Servers"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_8dot5_85() {
        pattern = "85";
        
        variants = asList(
                "Tools/Servers/Web_Servers/Apache_Tomcat/8.5.5",
                "Tools/Servers/Web_Servers/Apache_Tomcat/8.5.5/bin"
                );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }    
    
    @Test
    public void test_servers_1_sers() {
        pattern = "sers";
        
        variants = asList(
                "Tools/Servers",
                "Films/Serials",
                "Dev/Start_MySQL_server"
                );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }    
    
    @Test
    public void test_servers_2_sers() {
        pattern = "sers";
        
        variants = asList(
                "Tools/Servers",
                "Tools/Servers/Web_Servers"
                );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_photosCase_phots() {
        pattern = "Phots";
        
        variants = asList(
                "Projects",
                "Photos");
        
        expected = asList(
                "Photos");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_wizoriaCase_wiaora() {
        pattern = "wiaora";
        
        variants = asList(
                "Wizoria",
                "Projects/Diarsid/WebStorm/Node.js");
        
        expected = asList("Wizoria");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_nodejsProjectsCase_nojs() {
        pattern = "nojs";
        
        variants = asList(
                "Projects/Diarsid/WebStorm/Node.js",
                "Engines/Node.js/path");
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_searchSendingBeamCase_seaersengid() {
        pattern = "seaersengid";
        
        variants = asList(
                "Job/Search/for_sending",
                "Job/Search/Friends");
        
        expected = asList(
                "Job/Search/for_sending");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_ambientCase_aimbent() {
        pattern = "aimbent";
        
        variants = asList(
                "The_Hobbit_Calm_Ambient_Mix_by_Syneptic_Episode_II.mp3"
        );
        
        expected = asList(
                "The_Hobbit_Calm_Ambient_Mix_by_Syneptic_Episode_II.mp3");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_9thAgeCase_image() {
        pattern = "9age";
        
        variants = asList(
                "Image", 
                "Content/WH/Game/The_9th_Age"
        );
        
        expected = asList(
                "Content/WH/Game/The_9th_Age"
        );
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_9thAgeRostersCase_image() {
        pattern = "9agerost";
        
        variants = asList(
                "Content/WH/Game/The_9th_Age/Rosters"
        );
        
        expected = asList(
                "Content/WH/Game/The_9th_Age/Rosters"
        );
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_rostersCase_rester() {
        pattern = "rester";
        
        variants = asList(
                "Dev/Start_MySQL_server",
                "Music/2__Store/Therion",
                "Content/WH/Game/The_9th_Age/Rosters"
        );
        
        expected = asList(
                "Content/WH/Game/The_9th_Age/Rosters"
        );
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_rostersCase2_rester() {
        pattern = "rester";
        
        variants = asList(
                "Dev/Start_MySQL_server",
                "Content/WH/Game/The_9th_Age/Rosters/Elves.txt"
        );
        
        expected = asList(
                "Content/WH/Game/The_9th_Age/Rosters/Elves.txt"
        );
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_rostersCase3_roseter() {
        pattern = "roseter";
        
        variants = asList(
                "Content/WH/Game/The_9th_Age/Rosters/Elves.txt"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_rostersCase3_reostr() {
        pattern = "reostr";
        
        variants = asList(
                "Content/WH/Game/The_9th_Age/Rosters"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_sqlDeveloperCase_sldev() {
        pattern = "sldev";
        
        variants = asList(
                "Dev/Start_Tomcat",
                "Dev/Sql_Developer"
        );
        
        expected = asList(
                "Dev/Sql_Developer",
                "Dev/Start_Tomcat"         
        );
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_sqlDeveloperCase2_slde() {
        pattern = "slde";
        
        variants = asList(
                "Books/Common/Lem_S",
                "Dev/Sql_Developer"
        );
        
        expected = asList(
                "Dev/Sql_Developer"      
        );
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_sqlDeveloperCase3_slde() {
        pattern = "slde";
        
        variants = asList(
                "Films/Movies/Middle_Earth",
                "Dev/Sql_Developer"
        );
        
        expected = asList(
                "Dev/Sql_Developer"       
        );
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_tomcarotCase_tomcarot() {
        pattern = "tomcarot";
        
        variants = asList(
                "Dev/Start_Tomcat",
                "Tomcat root"
        );
        
        expected = asList(
                "Tomcat root",
                "Dev/Start_Tomcat"         
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_rostersCase_rosers() {
        pattern = "rosers";
        
        variants = asList(
                "Rosters", 
                "Projects/Diarsid"
        );
        
        expected = asList(
                "Rosters"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_musicListCase_muslit() {
        pattern = "muslit";
        
        variants = asList(
                "Music/List",
                "Programs/Links/util"
        );
        
        expected = asList(
                "Music/List"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsCase_porj() {
        pattern = "porj";
        
        variants = asList(
                "projects"
        );
        
        expected = asList(
                "projects"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_hobbitBookCase_hboitbok() {
        pattern = "hboitbok";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2"
        );
        
        expected = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_hobbitBookCase_bokhboit() {
        pattern = "bokhboit";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2"
        );
        
        expected = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_hobbitBookCase_hobitbok() {
        pattern = "hobitbok";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2"
        );
        
        expected = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_hobbitBookCase_hobot() {
        pattern = "hobot";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2",
                "Images/Photos"
        );
        
        expected = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_javaHomeCase_homjav() {
        pattern = "homjav";
        
        variants = asList(
                "Engines/Java/Path/JAVA_HOME/bin/java.exe",
                "Engines/Java/Path/JAVA_HOME/bin/java.spring.exe",
                "Engines/Java/Path/JAVA_HOME");
        
        expected = asList(
                "Engines/Java/Path/JAVA_HOME", 
                "Engines/Java/Path/JAVA_HOME/bin/java.exe",
                "Engines/Java/Path/JAVA_HOME/bin/java.spring.exe");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_javaHomeCase_javhom() {
        pattern = "javhom";
        
        variants = asList(
                "Engines/Java/Path/JAVA_HOME/bin/java.exe",
                "Engines/Java/Path/JAVA_HOME/bin/java.spring.exe",
                "Engines/Java/Path/JAVA_HOME");
        
        expected = asList(
                "Engines/Java/Path/JAVA_HOME", 
                "Engines/Java/Path/JAVA_HOME/bin/java.exe",
                "Engines/Java/Path/JAVA_HOME/bin/java.spring.exe");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_JavaTechCase_jatech() {
        pattern = "jatech";
        
        variants = asList(
                "Books/tech",
                "Tech",
                "Books/Tech/Design",
                "Tech/langs",
                "Books/Tech/Java");
        
        expected = asList( 
                "Books/Tech/Java");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_SoulProgramsCase_soulprogs() {
        pattern = "soulprogs";
        
        variants = asList(
                "Soul/programs/src",
                "Soul/programs"
        );
        
        expected = asList( 
                "Soul/programs",
                "Soul/programs/src");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_sqlDeveloperCase_slde() {
        pattern = "slde";
        
        variants = asList(
                "Dev/Sql_Developer");
        
        expected = asList( 
                "Dev/Sql_Developer");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_tolkienCase_tolknei() {
        pattern = "tolknei";
        
        variants = asList(
                "tolkien");
        
        expected = asList( 
                "tolkien");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_EarthMoviesCase_earhmives() {
        pattern = "earhmives";
        
        variants = asList(
                "Films/Movies/Middle_Earth/The_Hobbit",
                "Films/Movies/Middle_Earth");
        
        expected = asList( 
                "Films/Movies/Middle_Earth",
                "Films/Movies/Middle_Earth/The_Hobbit");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_DiarsidProjectsCase_diarsidprojecs() {
        pattern = "diarsidprojecs";
        
        variants = asList(
                "Projects/Diarsid",                
                "Projects/Diarsid/NetBeans");
        
        expected = asList(
                "Projects/Diarsid",
                "Projects/Diarsid/NetBeans");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_DiarsidProjectsCase_drsprojs() {
        pattern = "drsprojs";

        variants = asList(
                "Projects/Diarsid",
                "Projects/Diarsid/NetBeans");

        expected = asList(
                "Projects/Diarsid",
                "Projects/Diarsid/NetBeans");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_DiarsidProjectsCase_drsd() {
        pattern = "drsd";
        
        variants = asList(
                "Projects/Diarsid");
        
        expected = asList(
                "Projects/Diarsid");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_GitHubPagesCase_gihbpgs() {
        pattern = "gihbpgs";
        
        variants = asList(
                "X__GitHub_Pages");
        
        expected = asList(
                "X__GitHub_Pages");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_NetBeansCase_nebean() {
        pattern = "nebean";
        
        variants = asList(
                "Projects/Diarsid/NetBeans/Beam",                
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Research.Java",
                "Dev/NetBeans_8.2.lnk");
        
        expected = asList( 
                "Dev/NetBeans_8.2.lnk",
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Beam",
                "Projects/Diarsid/NetBeans/Research.Java");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_BeamProjectCase_beporj() {
        pattern = "beporj";
        
        variants = asList(                
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Beam"
        );
        
        expected = asList( 
                "Projects/Diarsid/NetBeans/Beam",                
                "Projects/Diarsid/NetBeans"
        );
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_NetBeansShortCase_nebean() {
        pattern = "nebean";
        
        variants = asList(               
                "Projects/Diarsid/NetBeans",
                "Dev/NetBeans");
        
        expected = asList( 
                "Dev/NetBeans",
                "Projects/Diarsid/NetBeans");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_NetBeansCase_nebena() {
        pattern = "nebena";
        
        variants = asList(
                "2__LIB/Maven_Local_Repo/io/springfox/springfox-bean-validators",
                "1__Projects/Diarsid/NetBeans"
        );
        
        expected = asList( 
                "1__Projects/Diarsid/NetBeans"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_NetBeansCase_nebaen() {
        pattern = "nebaen";
        
        variants = asList(
                "Projects/Diarsid/NetBeans/Beam",               
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Research.Java",
                "Dev/NetBeans_8.2.lnk"
        );
        
        expected = asList( 
                "Dev/NetBeans_8.2.lnk",
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Beam",
                "Projects/Diarsid/NetBeans/Research.Java");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_NetBeansCase_nebaen_noWorseThan() {
        pattern = "nebaen";
        
        variants = asList(
                "Projects/Diarsid/NetBeans/Beam",               
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Research.Java",
                "Dev/NetBeans_8.2.lnk"
        );
        
        noWorseThan = "Projects/Diarsid/NetBeans/Beam";
        
        expected = asList( 
                "Dev/NetBeans_8.2.lnk",
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Beam");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_NetBeansCase_single_nebaen() {
        pattern = "nebaen";
        
        variants = asList(
                "Projects/Diarsid/NetBeans/Beam");
               
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();      
    }      
                
    
    @Test
    public void test_NetBeansCase_nebaen_short() {
        pattern = "nebaen";
        
        variants = asList(
                "Projects/Diarsid/NetBeans/Beam", 
                "Dev/NetBeans_8.2.lnk");
        
        expected = asList( 
                "Dev/NetBeans_8.2.lnk",
                "Projects/Diarsid/NetBeans/Beam");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_NetBeansCase_nebaen8() {
        pattern = "nebaen8";
        
        variants = asList(
                "Projects/Diarsid/NetBeans/Beam",                
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Research.Java",
                "Dev/NetBeans_8.2.lnk");
        
        expected = asList( 
                "Dev/NetBeans_8.2.lnk",
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Beam",
                "Projects/Diarsid/NetBeans/Research.Java");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_beamProjectCase_beaporj() {
        pattern = "beaporj";
        
        variants = asList(
                "beam_project_home",
                "beam_project",
                "beam_home",
                "awesome java libs",
                "git>beam",
                "beam_project/src",
                "beam netpro",
                "abe_netpro",
                "babel_pro",
                "netbeans_projects", 
                "beam_server_project"
        );
        
        expected = asList( 
                "beam_project",
                "beam_project_home",
                "beam_server_project",
                "beam_project/src");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_beamProjectCase_beaporj_toRemove() {
        pattern = "beaporj";

        variants = asList(
                "beam netpro"
        );

        expected = asList();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projsdrs_full() {
        pattern = "projsdrs";

        variants = asList(
                "D:/DEV/1__Projects/Diarsid",
                "D:/DEV/1__Projects/Diarsid/src",
                "D:/DEV/1__Projects/Diarsid/Other",
                "D:/DEV/1__Projects/Diarsid/RStudio",
                "D:/DEV/1__Projects/Diarsid/IntelliJ",
                "D:/DEV/1__Projects/Diarsid/WebStorm",
                "D:/DEV/1__Projects/Diarsid/NetBeans",
                "D:/DEV/1__Projects/Diarsid/X__Reserve",
                "D:/DEV/1__Projects/Diarsid/X__Distrib",
                "D:/DEV/1__Projects/Diarsid/X__GitHub_Pages",
                "D:/DEV/1__Projects/X__Archive/Diarsid"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_dsrsdprojs_full() {
        pattern = "dsrsdprojs";

        variants = asList(
                "D:/DEV/1__Projects/Diarsid",
                "D:/DEV/1__Projects/Diarsid/src",
                "D:/DEV/1__Projects/Diarsid/Other",
                "D:/DEV/1__Projects/Diarsid/RStudio",
                "D:/DEV/1__Projects/Diarsid/IntelliJ",
                "D:/DEV/1__Projects/Diarsid/WebStorm",
                "D:/DEV/1__Projects/Diarsid/NetBeans",
                "D:/DEV/1__Projects/Diarsid/X__Reserve",
                "D:/DEV/1__Projects/Diarsid/X__Distrib",
                "D:/DEV/1__Projects/Diarsid/X__GitHub_Pages",
                "D:/DEV/1__Projects/X__Archive/Diarsid"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_dsrsdprojs_full_2() {
        pattern = "dsrsdprojs";

        variants = asList(
                "D:/DEV/1__Projects/Diarsid/X__Distrib",
                "D:/DEV/1__Projects/Diarsid/X__GitHub_Pages"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_dsrsdprojs() {
        pattern = "dsrsdprojs";

        variants = asList(
                "D:/DEV/1__Projects/Diarsid",
                "D:/DEV/1__Projects/Diarsid/NetBeans"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_drsdprojs_full() {
        pattern = "drsdprojs";

        variants = asList(
                "D:/DEV/1__Projects/Diarsid",
                "D:/DEV/1__Projects/Diarsid/src",
                "D:/DEV/1__Projects/Diarsid/Other",
                "D:/DEV/1__Projects/Diarsid/RStudio",
                "D:/DEV/1__Projects/Diarsid/IntelliJ",
                "D:/DEV/1__Projects/Diarsid/WebStorm",
                "D:/DEV/1__Projects/Diarsid/NetBeans",
                "D:/DEV/1__Projects/Diarsid/X__Reserve",
                "D:/DEV/1__Projects/Diarsid/X__Distrib",
                "D:/DEV/1__Projects/Diarsid/X__GitHub_Pages",
                "D:/DEV/1__Projects/X__Archive/Diarsid"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_drsdprojs_DiarsidDistrib() {
        pattern = "drsdprojs";

        variants = asList(
                "D:/DEV/1__Projects/Diarsid/X__Distrib"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_porjdrsd_full() {
        pattern = "porjdrsd";

        variants = asList(
                "D:/DEV/1__Projects/Diarsid",
                "D:/DEV/1__Projects/Diarsid/src",
                "D:/DEV/1__Projects/Diarsid/Other",
                "D:/DEV/1__Projects/Diarsid/RStudio",
                "D:/DEV/1__Projects/Diarsid/IntelliJ",
                "D:/DEV/1__Projects/Diarsid/WebStorm",
                "D:/DEV/1__Projects/Diarsid/NetBeans",
                "D:/DEV/1__Projects/Diarsid/X__Reserve",
                "D:/DEV/1__Projects/Diarsid/X__Distrib",
                "D:/DEV/1__Projects/Diarsid/X__GitHub_Pages",
                "D:/DEV/1__Projects/X__Archive/Diarsid"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_porjsdrsd() {
        pattern = "porjsdrsd";

        variants = asList(
                "D:/DEV/1__Projects/Diarsid"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_drs_full() {
        pattern = "drs";

        variants = asList(
                "D:/DEV/1__Projects/Diarsid"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_drs() {
        pattern = "drs";

        variants = asList(
                "D:/DEV/1__Projects/Diarsid",
                "D:/DEV/1__Projects/Diarsid/Some_other_words"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projsdrs() {
        pattern = "projsdrs";

        variants = asList(
                "D:/DEV/1__Projects/Diarsid"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_JavaSE8Case_jse8() {
        pattern = "jse8";
        
        variants = asList(               
                "Java SE 8 API",
                "Job/Search"
        );
        
        expected = asList( 
                "Java SE 8 API"
        );
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_jpaSpecCase_jpaspce() {
        pattern = "jpaspce";
        
        variants = asList(               
                "Java/Specifications",
                "Java/Specifications/JPA_v.2.0_(JSR_317).pdf"
        );
        
        expected = asList(            
                "Java/Specifications/JPA_v.2.0_(JSR_317).pdf",
                "Java/Specifications"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_beamProjectCase_beaporj_2() {
        pattern = "beaporj";
        
        variants = asList( 
                "beam_server_project"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_beamProjectCase_short_beaporj() {
        pattern = "beaporj";
        
        variants = asList(
                "beam_project_home",
                "beam_server_project");
        
        expected = asList(
                "beam_project_home",
                "beam_server_project");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_differens_tols() {
        
        pattern = "tols";
        
        variants = asList(
                "Tools",
                "Images/Photos",
                "Music/2__Store",
                "Projects",
                "Torrents",
                "Books/Common/Tolkien_J.R.R"
        );
        
        expected = asList(
                "Tools");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_differens_tolos() {

        pattern = "tolos";

        variants = asList(
                "Tools_aaaaa",
                "Tools_to",
                "Tools_aaaaa",
                "Tools_looking",
                "book_tolstoy",
                "Tools",
                "tolkien_lost",
                "lost_old_to",
                "to_low_losing",
                "topolski"
        );

        expected = asList(
                "Tools",
                "Tools_to",
                "Tools_aaaaa",
                "Tools_aaaaa",
                "Tools_looking",
                "lost_old_to",
                "tolkien_lost",
                "to_low_losing",
                "book_tolstoy"
                );

        worseVariantsDontMatter();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_servs() {

        pattern = "servs";

        variants = asList(
                "supervision",
                "servers"
        );

        expected = asList(
                "servers",
                "supervision"
                );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_srv() {

        pattern = "srv";

        variants = asList(
                "supervision",
                "servers"
        );

        expected = asList(
                "servers",
                "supervision"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_jeschrstpassn() {

        pattern = "jeschrstpassn";

        variants = asList(
                "The Passion of Jesus Christ by John Piper",
                "The Chronicles of Chrestomanci by Diana Wynne Jones"
        );

        expected = asList(
                "The Passion of Jesus Christ by John Piper"
        );

        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_enginesJavaBinCase_engjbin() {
        pattern = "engjbin";
        
        variants = asList(
                "Engines/Java/Path/JAVA_HOME/bin/java.exe",
                "Engines/Java/Path/JAVA_HOME/bin"
        );
        
        expected = asList(
                "Engines/Java/Path/JAVA_HOME/bin",
                "Engines/Java/Path/JAVA_HOME/bin/java.exe");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_matrix_philosophy_mtrxpholspy() {
        pattern = "mtrxpholspy";

        variants = asList(
                "The Matrix and Philosophy: Welcome to the Desert of the Real"
        );

        expected = asList(
                "The Matrix and Philosophy: Welcome to the Desert of the Real"
        );

        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_jobCurrentCase_jbo_cruent_with_separator() {
        pattern = "jbo/cruent";
        
        variants = asList(
                "Job/Current",
                "Current_Job/Hiring/CVs"
        );
        
        expected = asList(
                "Job/Current",
                "Current_Job/Hiring/CVs");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_jobCurrentCase_jbo_cruent() {
        pattern = "jbocruent";

        variants = asList(
                "Job/Current",
                "Current_Job/Hiring/CVs"
        );

        expected = asList(
                "Job/Current",
                "Current_Job/Hiring/CVs");

        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_toolsCase_tols() {
        pattern = "tols";
        
        variants = asList(
                "LostFilm", 
                "Dev/3__Tools"
        );
        
        expected = asList( 
                "Dev/3__Tools"
        );
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_UkrposhtaStatusNotificationServiceClient_ukrptnotfclntsts() {
        pattern = "ukrptnotfclntsts";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationService",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationGenerator"
        );

        expected = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_UkrposhtaStatusNotificationServiceClient_ukrptnotfclntsts_1() {
        pattern = "ukrptnotfclntsts";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient"
        );

        expected = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_UkrposhtaStatusNotificationServiceClient_ukrptnotfclntstts_1() {
        pattern = "ukrptnotfclntstts";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient"
        );

        expected = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_UkrposhtaStatusNotificationServiceClient_upshstnoftftclnt() {
        pattern = "upshstnoftftclnt";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationService",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationGenerator"
        );

        expected = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_tolkien_0_lorofrngbyjrrtolk() {

        pattern = "lorofrngbyjrrtolk";

        variants = asList(
                "The Fellowship Of The Ring (Turtleback School & Library Binding Edition) (Lord of the Rings) by J.R.R. Tolkien",
                "Lord of the Rings by J.R.R Tolkien",
                "The Return of the King (The Lord of the Rings #3) by Rob Inglis and J.R.R. Tolkien, and Brian Sibley"
        );

        expected = asList(
                "Lord of the Rings by J.R.R Tolkien",
                "The Return of the King (The Lord of the Rings #3) by Rob Inglis and J.R.R. Tolkien, and Brian Sibley",
                "The Fellowship Of The Ring (Turtleback School & Library Binding Edition) (Lord of the Rings) by J.R.R. Tolkien"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_tolkien_lororng() {

        pattern = "lororng";

        variants = asList(
                "The Lord of the Rings ",
                "Lord of the Rings by J.R.R Tolkien ",
                "The Lord of the Rings: The Art of the Fellowship of the Ring by Gary Russell"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_ecom_posecom() {

        pattern = "posecom";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaEcom_1",
                "The Complete Poems and Major Prose by John Milton and Merritt Y. Hughes "
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }



    @Test
    public void test_tolkien_1_lorofrngbyjrrtolk() {

        pattern = "lorofrngbyjrrtolk";

        variants = asList(
                "The Return of the King (The Lord of the Rings #3) by Rob Inglis and J.R.R. Tolkien, and Brian Sibley"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_TolkienCase_tol() {
        pattern = "tol";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R", 
                "Dev/3__Tools");
        
        expected = asList( 
                "Books/Common/Tolkien_J.R.R",
                "Dev/3__Tools");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_javaPathCase_jpath() {
        pattern = "jpath";
        
        variants = asList(
                "Engines/java/path", 
                "Books/Tech/Java/JavaFX", 
                "Books/Tech/Java");
    
        expected = asList( 
                "Engines/java/path");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_javaPathCase2_jpath() {
        pattern = "jpath";
        
        variants = asList(
                "Engines/Java/Path",
                "Engines/Java/Path/Jshell",
                "Engines/Java/Path/JAVA_HOME/bin"
                );
    
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_javaSpecCase_jspec() {
        pattern = "jspec";
        
        variants = asList(                
                "Projects/UkrPoshta/UkrPostAPI",
                "Tech/langs/Java/Specifications");
    
        expected = asList( 
                "Tech/langs/Java/Specifications");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_JavaBooksCase_jboks() {
        pattern = "jboks";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R",
                "Current_Job/Workspace",
                "Books/Tech/Java");
        
        expected = asList(
                "Books/Tech/Java",
                "Books/Common/Tolkien_J.R.R");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_commonBooksCase_comboks() {
        pattern = "comboks";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R",
                "Books/Common");
        
        expected = asList( 
                "Books/Common",
                "Books/Common/Tolkien_J.R.R");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_commonBooksCase_commbooks() {
        pattern = "commbooks";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R",
                "Books/Common");
        
        expected = asList( 
                "Books/Common",
                "Books/Common/Tolkien_J.R.R");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_ukrposapi() {
        pattern = "ukrposapi";
        
        variants = asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_ukropsapi() {
        pattern = "ukropsapi";
        
        variants = asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta"
        );
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projectsUkrPoshtaCainiao_ukropsapi() {
        pattern = "ukropsapi";

        variants = asList(
                "Projects/UkrPoshta/CainiaoAPI");

        expected = asList(
                "Projects/UkrPoshta/CainiaoAPI"
        );

        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_ukrpso() {
        pattern = "ukrpso";
        
        variants = asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_posthproj() {
        pattern = "posthproj";
        
        variants = asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/UkrPostAPI"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_ukrpsoht() {
        pattern = "ukrpsoht";
        
        variants = asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI");
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projectsUkrPoshta_poshtapiukr() {
        pattern = "poshtapiukr";

        variants = asList(
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta");

        expected = asList(
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projectsUkrPoshta_poshtapiukr_2() {
        pattern = "poshtapiukr";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrPoshta_API");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projectsUkrPoshta_ukrposhtarchiv() {
        pattern = "ukrposhtarchiv";

        variants = asList(
                "D:/DEV/1__Projects/X__Archive/UkrPoshta",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusTracking");

        expected = asList("D:/DEV/1__Projects/X__Archive/UkrPoshta");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_disrdarcv() {
        pattern = "disrdarcv";

        variants = asList(
                "D:/DEV/1__Projects/X__Archive/Diarsid");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_disrdarcv_mass() {
        pattern = "disrdarcv";

        variants = asList("Vanishing Ireland: Further Chronicles of a Disappearing World by Turtle Bunbury",
                "Parkinson's Disease and Movement Disorders by Dr. Joseph Jankovic MD",
                "Disparitions by Natsuo Kirino",
                "Discovery Channel The Big Book of Sharks by Discovery Channel",
                "Diseases and Disorders - Birth Defects by Barbara Sheen",
                "The Logic of Scientific Discovery (Routledge Classics) by Karl Popper",
                "Diary of a Minecraft Zombie Book 1: A Scare of A Dare (Volume 1) by Herobrine Books",
                "Life Reimagined: Discovering Your New Life Possibilities by Richard J. Leider",
                "Oils and Vinegars: Discovering and Enjoying Gourmet Oils and Vinegars by Liz Franklin",
                "Blame It on the Brain: Distinguishing Chemical Imbalances Brain Disorders and Disobedience by Edward T. Welch",
                "Kokology: The Game of Self-Discovery by Tadahiko Nagao",
                "Maternal and Newborn Success: A Q&A Review Applying Critical Thinking to Test Taking (Davis's Success) by Margot R. De Sevo PhD LCCE IBCLC RNC",
                "Colours of Survival: Discovering Hope in Bangladesh by Adrian Plass",
                "Manic-Depressive Illness: Bipolar Disorders and Recurrent Depression by Frederick K. Goodwin and Kay Redfield Jamison",
                "The Bible Cure for Skin Disorders: Ancient Truths, Natural Remedies and the Latest Findings for Your Health Today (New Bible Cure (Siloam)) by Don Colbert MD",
                "The Discomfort Zone: A Personal History by Jonathan Franzen",
                "The Discomfort Zone: A Personal Journey by Jonathan Franzen",
                "Tsunamis and Other Natural Disasters (Magic Tree House Research Guide #15) by Salvatore Murdocca and Natalie Pope Boyce, and Mary Pope Osborne",
                "No Limits: Viewers Discretion Advised. (Volume 1)",
                "D:/DEV/1__Projects/X__Archive/Diarsid",
                "$30 Film School: How to Write Direct Produce Shoot Edit Distribute Tour With and Sell Your Own No-Budget Digital Movie by Michael W. Dean",
                "The Thirteen Original Clan Mothers: Your Sacred Path to Discovering the Gifts, Talents, and Abilities of the Feminine Through the Ancient Teachings of the Sisterhood by Jamie Sams",
                "No Disrespect by Sister Souljah",
                "This Is Tanzania: The diary of an Englishman in Africa by Mr Adrian Francis Strain",
                "McCarthy's Bar: A Journey of Discovery in Ireland by Pete McCarthy",
                "Our Origins: Discovering Physical Anthropology (Second Edition) by Clark Spencer Larsen",
                "Discovering Sign Language by Laura Greene",
                "Bipolar Disorders: Basic Mechanisms and Therapeutic Implications (Medical Psychiatry Series)",
                "Bringing Up Bébé: One American Mother Discovers the Wisdom of French Parenting (now with Bébé Day by Day: 100 Keys to French Parenting) by Pamela Druckerman",
                "Master The Public Safety Dispatcher/911 Operator Exam: Targeted Test Prep to Jump-Start Your Career by Peterson's",
                "Learning Pandas - Python Data Discovery and Analysis Made Easy by Michael Heydt",
                "Discovery of the Presence of God: Devotional Nonduality by David R. Hawkins",
                "Discovering Geometry: An Investigative Approach by Michael Serra",
                "Discovering Computers & Microsoft Office 2013: A Fundamental Combined Approach (Shelly Cashman Series) by Misty E. Vermaat",
                "Healing the New Childhood Epidemics: Autism, ADHD, Asthma, and Allergies: The Groundbreaking Program for the 4-A Disorders by Kenneth Bock",
                "Ramus, Method, and the Decay of Dialogue: From the Art of Discourse to the Art of Reason by Walter J. Ong S.J.",
                "The Queen of Distraction: How Women with ADHD Can Conquer Chaos, Find Focus, and Get More Done by Terry Matlen",
                "The Globe Encompassed: The Age of European Discovery (1500 to 1700) by Glenn J. Ames",
                "Wild at Heart: Discovering the Secret of a Man's Soul by John Eldredge",
                "Time's Arrow Time's Cycle: Myth and Metaphor in the Discovery of Geological Time by Stephen Jay Gould",
                "Do What You Are: Discover the Perfect Career for You Through the Secrets of Personality Type by Paul D. Tieger",
                "The Kuwaiti Oil Fires (Environmental Disasters) by Kristine Hirschmann",
                "The Piano Shop on the Left Bank: Discovering a Forgotten Passion in a Paris Atelier by Thad Carhart",
                "What Color Is Your Parachute? For Teens, 2nd Edition: Discovering Yourself, Defining Your Future by Carol Christen",
                "World Religions (2015): A Voyage of Discovery 4th Edition by Jeffrey Brodd",
                "Distinctive Presentations In Needle Art: A Complete Guide to Professional Finishing for Your Needlework by Marcia S. Brown",
                "Discourses Books 3-4. The Enchiridion (Loeb Classical Library #218) by Epictetus and William A. Oldfather",
                "Sharpen Your Discernment by Roberts Liardon",
                "Dispatches from the Edge: A Memoir of War Disasters and Survival by Anderson Cooper",
                "Amber and Ashes (Dragonlance: The Dark Disciple #1) by Margaret Weis",
                "Nabokov's Pale Fire: The Magic of Artistic Discovery by Brian Boyd",
                "OriginsThe term originated from the Bene Gesserit's prescient powers, and their inability to venture into a specific region of prescient knowledge. This region, though mysterious in nature, was known to be unattainable to females. Specifically, the spice melange allowed the Bene Gesserit to unlock genetic memory, but only on their maternal side. Female humans carry two X chromosomes, while males possess an X chromosome and a Y chromosome. A Kwisatz Haderach would be capable of accessing genetic memories stored within both chromosomes, while those memories were inaccessible to Bene Gesserit. Memories from male ancestors are still accessible to females with access to genetic memory (Alia is able to make contact with the ego memory of Vladimir Harkonnen for instance) but due to the lack of the Y chromosome, the memories were incomplete. Accessing the full paternal memories was impossible for the Bene Gesserit, and the very thought of trying was terrifying to them. Further, melange also exposed the Sisterhood (and others) to a limited form of prescience - thoughts, feelings, images into the near future, but no more.Bene Gesserit AttemptThe Bene Gesserit desire to uncover this knowledge and its associated powers, drove them to initiate a long-running genetic breeding program. This would yield a male with mental powers capable of bridging space and time and that he would be under the direct control of the Sisterhood. The Bene Gesserit knew that not only would their Kwisatz Haderach possess Other Memory on both the male and female side, but that he would be able to predict the future precisely. He would be Bene Gesserit Reverend Mother, Mentat, and Guild Navigator, all in one.After the emergence of this male - Paul Atreides - the term Kwisatz Haderach was also understood as meaning \"one who can be many places at once\", and became synonymous with Paul. In time, it also came to encompass his sister, Alia Atreides, and his son Leto Atreides II, since they all had similar abilities.The initial Bene Gesserit plan was to breed the daughter of Duke Leto Atreides to a Harkonnen male, which would produce the Kwisatz Haderach. This was to be the culmination of more than 10,000 years of careful breeding. This plan would have seen the end of the centuries-old feud between the Great Houses Atreides and Harkonnen, and placed a prescient, Bene Gesserit-controlled male on the Golden Lion Throne, the Kwisatz Haderach. However, because of her love for Duke Leto, the Lady Jessica disobeyed her fellow Bene Gesserit and gave birth to a son instead of a daughter, to give him an heir, Paul.It seemed fairly certain from early in Paul's life that he would in fact be the Kwisatz Haderach, since he showed an ability to see into the future. It was when he was fifteen years of age that the Bene Gesserit sent a Reverend Mother to test Paul's prescience and his training in the Bene Gesserit ways. This event appeared to be a significant catalyst for the events that would befall the universe for the next several thousand years. Paul's testing with the Gom Jabbar, as well as the Sisterhood's silent complicity in his father's death, proved to instill significant negativity in Paul against the Sisterhood. As a result, when he reached young adulthood, and ascended to the Golden Lion Throne, he vowed that he would never be under the control of the Sisterhood. Because of this, the Sisterhood lost control of their breeding program, their Kwisatz Haderach, and the possibility of placing a Bene Gesserit on the throne.Paul Leaves the Golden PathIndeed, it appeared that the majority of people did not realize that to know the future is to be trapped by it. Paul could see that he would have to lead humanity onto a drastic course in order for it to escape its own annihilation. As a result, he chose to escape the Golden Path by allowing himself to be blinded, so that he could walk into the desert and not be a burden on his tribe. The mantle of the Golden Path was subsequently taken up by Leto II.ImpactAs Kwisatz Haderach, both Paul and his son Leto would at times experience significant grief because they knew they were trapped by their own destinies. One of the few people who understood this was in fact one of Paul's enemies. It was through the failed Tleilaxu Kwisatz Haderach 'experiment' that the Tleilaxu Master Scytale could in fact sympathize with Paul and the decisions he had to make.When Leto II, the next Kwisatz Haderach, ascended to the throne, not only did he manage to avoid Bene Gesserit control, but he also took control of their breeding program. He then relegated them to a relatively insignificant role in the universe throughout the 3500 years of his reign.The ScatteringAfter the Famine Times prompted by Leto II after his death. The remaining power brokers in the Old Empire either forgot about the concept of the Kwisatz, or vehemently guarded against the rise of another of the super-beings. The Bene Gesserit would go so far as to terminate the life of humans that showed aspects of enhanced abilities. But as was shown with the \"wild\" Atreides line, the heightened human abilities continually showed themselves.After the The Scattering, both Miles Teg and his daughter Darwi Odrade, descendants of the Atreides, showed super-normal actions. Teg was able to move at lightning-fast speed and disabled his kidnappers on Gammu. Odrade had limited prescience that enabled her to know that the misguided Honored Matres and Bene Gesserit should merge to form one single organization.But it was the ghola of Duncan Idaho which caused the most concern for all involved. The conservative branch of the Sisterhood, led by Schwangyu, would go so far as to disobey the order of the Mother Superior Alma Mavis Taraza, and terminate the life of a young Duncan ghola -- even as she has sworn to be his protector from the Tleilaxu, who had allied themselves temporarily with the returning Honored Matres.Behind the scenesIt's likely that the term was borrowed by Frank Herbert from the Kabbala. The compilation of Jewish mysticism the core of which is entitled HaZohar (The Book of Splendor) ascribed to Rabbi Shimon bar-Yohai, ascribed by Jews to have been written in the 1st century C.E. Modern scholars ascribe it to a sephardic Jewish author around the 13th century. The term itself is Hebrew, \"K\"fitzat ha-Derekh\", (קְפִיצַת הַדֶּרֶךְ) literally, \"The Leap of the Way,\" by means of which an initiate may travel some distance instantaneously, appearing to be in two or more places at once.",
                "Career choices and changes: A guide for discovering who you are, what you want, and how to get it by Mindy Bingham",
                "Creativity: The Psychology of Discovery and Invention by Mihaly Csikszentmihalyi",
                "A Discourse on the Method (Oxford World's Classics) by René Descartes",
                "The Discovery of New Worlds (Story of the World #2) by Margaret Bertha Synge",
                "Age of Distraction: The Island by Patricia Mahon",
                "Neurodegenerative Disorders (Perspectives on Diseases and Disorders) by Sylvia Engdahl",
                "Disordered Minds by Minette Walters",
                "From the Age of Discovery to a World at War (America: The Last Best Hope #1) by William J. Bennett",
                "The National Geographic Society: 100 Years of Adventure & Discovery by C.D.B. Bryan",
                "Discovering God's Will by Sinclair B. Ferguson",
                "Unified Protocol for Transdiagnostic Treatment of Emotional Disorders: Therapist Guide (Treatments That Work) by David H. Barlow",
                "Discourse on Method and Meditations on First Philosophy by Donald A. Cress and René Descartes",
                "Discovering Knowledge in Data: An Introduction to Data Mining (Wiley Series on Methods and Applications in Data Mining) by Daniel T. Larose",
                "The Rhythm of Family: Discovering a Sense of Wonder through the Seasons by Amanda Blake Soule",
                "The Social Contract and The First and Second Discourses",
                "Be the Person You Want to Find: Relationship and Self-Discovery by June Shiver and Cheri Huber",
                "The Discovery (Dive #1) by Gordon Korman",
                "Discovering Great Artists: Hands-On Art for Children in the Styles of the Great Masters by Kim Solga and MaryAnn F. Kohl, and Rebecca Van Slyke",
                "Discourse on the Origin of Inequality by Jean-Jacques Rousseau",
                "Archaeology, Sexism, and Scandal: The Long-Suppressed Story of One Woman's Discoveries and the Man Who Stole Credit for Them by Alan Kaiser",
                "La disparition by Georges Perec",
                "Kicking Up Dirt: A True Story of Determination, Deafness, and Daring by Ashley Fiolek",
                "Jeff Shaara's Civil War Battlefields: Discovering America's Hallowed Ground by Jeff Shaara",
                "The Mammoth Book of Storms Shipwrecks and Sea Disasters by Richard Russell Lawrence",
                "New Worlds Ancient Texts: The Power of Tradition and the Shock of Discovery by Anthony Grafton",
                "National Geographic 125 Years: Legendary Photographs, Adventures, and Discoveries That Changed the World by Mark Collins Jenkins",
                "PMP Exam Success Series: MP3 Audio Flashcards and Discovering the PMBOK Guide",
                "Yosemite: Its Discovery Its Wonder and Its People by Margaret Sanborn",
                "Myth & the Movies: Discovering the Myth Structure of 50 Unforgettable Films by Stuart Voytilla");

        expected = asList("D:/DEV/1__Projects/X__Archive/Diarsid");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_OOB() {
        pattern = "conhelth";

        variants = asList("The Closers (Harry Bosch #11; Harry Bosch Universe #14) by Len Cariou and Michael Connelly",
                "Exemplary Novels IV: Lady Cornelia the Deceitful Marriage the Dialogue of the Dogs by Miguel de Cervantes Saavedra and John Macklin",
                "Tales of Sinanju: The Destroyer, book one \"Cooking Lesson\" by Muhammad Rasheed",
                "Where the Red Fern Grows with Connections",
                "Woe is I: The Grammarphobe's Guide to Better English in Plain English, 3rd Edition by Patricia T. O'Conner",
                "Independent Luxury: The Four Innovation Strategies To Endure In The Consolidation Jungle by Jonas Hoffmann",
                "The Confessions of Nat Turner by William Styron",
                "The Stepdad's Guide: Resolving Family Conflict by S. James Wheeler",
                "Moonlight in Duneland: The Illustrated Story of the Chicago South Shore and South Bend Railroad (Quarry Books) by Ronald D. Cohen",
                "The Sound and the Fury: An Authoritative Text Backgrounds and Contexts Criticism",
                "The Control of Nature by John McPhee",
                "The Black Ice (Harry Bosch #2; Harry Bosch Universe #2) by Michael Connelly",
                "Star Wars: Clone Wars Volume 6: On the Fields of Battle by Jan Duursema and John Ostrander",
                "The Concrete Blonde (Harry Bosch #3) by Dick Hill and Michael Connelly",
                "The Truth About Methamphetamine and Crystal Meth (Drugs & Consequences) by Lara Norquist",
                "A Friend of the Earth by T. Coraghessan Boyle",
                "Evidence-Based Nursing: The Research-Practice Connection by Sarah Jo Brown",
                "Fast Boat to China: High-Tech Outsourcing and the Consequences of Free Trade: Lessons from Shanghai by Andrew Ross",
                "The Chocolate-Covered Contest (Nancy Drew #151) by Carolyn Keene",
                "From the Corner of His Eye by Stephen Lang and Dean R. Koontz",
                "Small Talk: How to Connect Effortlessly With Anyone, Strike Up Conversations with Confidence and Make Small Talk Without the Fear of Being Awkward by Betty Bohm",
                "Social Sustainability in Urban Areas: Communities, Connectivity and the Urban Fabric",
                "Counseling Families Across the Stages of Life: A Handbook for Pastors and Other Helping Professionals by Andrew J. Weaver",
                "The Story of Philosophy: A Concise Introduction to the World's Greatest Thinkers and Their Ideas by Bryan Magee",
                "The Authentic Death and Contentious Afterlife of Pat Garrett and Billy the Kid: The Untold Story of Peckinpah's Last Western Film by Paul Seydor",
                "The Scarpetta Collection: All That Remains / Cruel & Unusual (Kay Scarpetta #3 #4) by Patricia Daniels Cornwell",
                "Extraordinary Popular Delusions and the Madness of Crowds/Confusión de Confusiones (Marketplace Book) by Charles Mackay and Martin S. Fridson, and Joseph de La Vega",
                "Cooking Light First Foods: Baby Steps to a Lifetime of Healthy Eating by Editors of Cooking Light Magazine",
                "The Big Empty: Dialogues on Politics Sex God Boxing Morality Myth Poker & Bad Conscience in America by John Buffalo Mailer and Norman Mailer",
                "The Confidence-Man by Tony Tanner and John Dugdale, and Herman Melville",
                "A Sorrow Shared: A Combined Edition of the Nouwen Classics \"In Memoriam\" and \"A Letter of Consolation\" by Henri J. M. Nouwen",
                "Emergence: The Connected Lives of Ants Brains Cities and Software by Steven Johnson",
                "299 Days: The 43 Colonels (Volume 10) by Glen Tate",
                "The Last Kingdom (The Saxon Stories #1) by Bernard Cornwell and Jamie Glover, and Tom Sellwood",
                "A Century in Captivity: The Life and Trials of Prince Mortimer, a Connecticut Slave (Revisiting New England) by Denis R. Caron",
                "Philosophical Investigations into the Essence of Human Freedom (Suny Series in Contemporary Continental Philosophy) by F. W. J. Schelling",
                "National Audubon Society Pocket Guide to Constellations of the Northern Skies (National Audubon Society Pocket Guides) by Mark Chartrand",
                "The Frog King by Frank McConnell",
                "The Rule of the Secular Franciscan Order: With a Catechism and Instructions by Cornelio Mota Ramos OFM",
                "Meeting Jesus Again for the First Time: The Historical Jesus and the Heart of Contemporary Faith by Marcus J. Borg",
                "Mental Health Concepts and Techniques for the Occupational Therapy Assistant (Point (Lippincott Williams & Wilkins)) by Mary Beth Early MS OTR",
                "Hard Days Hard Nights: From the Beatles to the Doors to the Stones... Insider Stories From a Legendary Concert Promoter by Pat DiCesare",
                "Darwin's Dangerous Idea: Evolution and the Meanings of Life by Daniel C. Dennett",
                "Reinventing Jesus: How Contemporary Skeptics Miss the Real Jesus and Mislead Popular Culture by Daniel B. Wallace and J. Ed Komoszewski, and M. James Sawyer",
                "Outrunning the Bear: How You Can Outperform Stocks and Bonds with Convertibles by Greg Miller",
                "William Shakespeare's The Clone Army Attacketh: Star Wars Part the Second (William Shakespeare's Star Wars) by Ian Doescher",
                "Every Tongue Got to Confess: Negro Folk-tales from the Gulf States by John Edgar Wideman and Zora Neale Hurston, and Carla Kaplan",
                "Kinds of Minds: Towards an Understanding of Consciousness by Daniel C. Dennett",
                "Who Needs Greek? Contests in the Cultural History of Hellenism by Simon Goldhill",
                "The Vintage Book of Contemporary American Short Stories by Tobias Wolff",
                "Chasing the Dime by Michael Connelly",
                "The Harry Bosch Novels Volume 1: The Black Echo / The Black Ice / The Concrete Blonde (Harry Bosch #1-3) by Michael Connelly",
                "How to Speak Brit: The Quintessential Guide to the King's English, Cockney Slang, and Other Flummoxing British Phrases by Christopher J. Moore",
                "The Light in Cuban Eyes: Lake Forest College's Madeleine P. Plonsker Collection of Contemporary Cuban Photography by Lake Forest College",
                "The Medical Advisor: The Complete Guide to Alternative & Conventional Treatments : Home Edition",
                "Star Wars: Clone Wars Volume 1: The Defense of Kamino and Other Tales by John Ostrander and W. Haden Blackman",
                "Awakening Through the Tears: Interstitial Cystitis and the Mind/Body/Spirit Connection by Catherine M. Simone",
                "Darwin's Watch (The Science of Discworld #3) by Ian Stewart and Jack Cohen, and Terry Pratchett",
                "The Cornell School of Hotel Administration on Hospitality: Cutting Edge Thinking and Practice",
                "Leaps of Faith: Science Miracles & the Search for Supernatural Consolation by Nicholas Humphrey and Daniel C. Dennett",
                "The Peacemaker: A Biblical Guide to Resolving Personal Conflict by Ken Sande",
                "You: The Owner's Manual: An Insider's Guide to the Body That Will Make You Healthier and Younger by Mehmet C. Oz and Michael F. Roizen",
                "The Dirt: Confessions of the World's Most Notorious Rock Band",
                "Star Wars: Clone Wars Volume 8: The Last Siege the Final Truth by Dan Parsons and Jan Duursema, and John Ostrander",
                "The Poetry of Robert Frost: The Collected Poems Complete and Unabridged by Edward Connery Lathem and Robert Frost",
                "D:/CONTENT/Health",
                "The Barefoot Contessa Cookbook by Melanie Acevedo and Ina Garten",
                "The Shadow Party: How George Soros Hillary Clinton and Sixties Radicals Seized Control of the Democratic Party by David Horowitz and Richard Poe",
                "Breaking Open the Head: A Psychedelic Journey Into the Heart of Contemporary Shamanism by Lee Fukui and Daniel Pinchbeck",
                "The Women's Basketball Drill Book (The Drill Book Series) by Women's Basketball Coaches Association",
                "The Review of Contemporary Fiction: Fall 2001: Gilbert Sorrentino/William Gaddis/Mary Caponegro/Margery Latimer",
                "Connecticut Off the Beaten Path, 5th: A Guide to Unique Places (Off the Beaten Path Series) by David Ritchie",
                "Sea Magic: Connecting with the Ocean's Energy by Sandra Kynes",
                "Electric Pressure Cooker Cookbook: 50 The Best Pressure Cooker Recipes-Prepare Food 70% Faster Than Conventional Cooking (Electric Pressure Cooker ... Cooker Recipes, Pressure Cooker Cookbook) by Joelyn Mckeown",
                "On the Threshold of Hope (Aacc Counseling Library) by Diane Mandt Langberg",
                "How to Prepare for the GED® Test (with CD-ROM): All New Content for the Computerized 2014 Exam (Barron's Ged (Book & CD-Rom)) by Christopher Sharpe",
                "Taking Charge of Your Fertility: The Definitive Guide to Natural Birth Control, Pregnancy Achievement, and Reproductive Health (Revised Edition) by Toni Weschler",
                "The Twilight War: The Secret History of America's Thirty-Year Conflict with Iran by David Crist",
                "The Great Thoughts, From Abelard to Zola, from Ancient Greece to Contemporary America, the Ideas that have Shaped the History of the World by George Seldes",
                "The FFT: Fundamentals and Concepts by Robert W. Ramirez",
                "Manating the School Age Child with a Chronic Health Condition (A Practical Guide for Schools, Families and Organizations) by Georgianna Larson",
                "The Confessions (Works of Saint Augustine 1) by John E. Rotelle and Maria Boulding, and Augustine of Hippo",
                "Law Enforcement In The United States by James A. Conser",
                "Dime-Store Alchemy: The Art of Joseph Cornell by Charles Simic",
                "Everybody Was Kung Fu Fighting: Afro-Asian Connections and the Myth of Cultural Purity by Vijay Prashad",
                "Elbow Room: The Varieties of Free Will Worth Wanting by Daniel C. Dennett",
                "The Chemistry of Mind-Altering Drugs: History, Pharmacology, and Cultural Context (American Chemical Society Publication) by Daniel M. Perrine",
                "D:/CONTENT/Health/Psy",
                "The Power of Vulnerability: Teachings on Authenticity, Connection and Courage by Brene Brown",
                "Still Only One Earth: Progress in the 40 Years Since the First UN Conference on the Environment (Issues in Environmental Science and Technology)",
                "When Santa Fell to Earth by Michael Paul Howard and Cornelia Funke, and Oliver G. Latsch",
                "Breaking the Spell: Religion as a Natural Phenomenon by Daniel C. Dennett",
                "Abandoned Parents: The Devil's Dilemma: The Causes and Consequences of Adult Children Abandoning Their Parents by Sharon A Wildey",
                "Democracy Matters: Winning the Fight Against Imperialism by Cornel West",
                "Ride of the Second Horseman: The Birth and Death of War by Robert L. O'Connell",
                "Ghosthunters and the Muddy Monster of Doom! (Ghosthunters #4) by Helena Ragg-Kirkby and Cornelia Funke",
                "Media Control: The Spectacular Achievements of Propaganda by Noam Chomsky",
                "Sport Riding Techniques: How To Develop Real World Skills for Speed, Safety, and Confidence on the Street and Track by Nick Ienatsch",
                "The Queen of Distraction: How Women with ADHD Can Conquer Chaos, Find Focus, and Get More Done by Terry Matlen",
                "The Patricia Cornwell CD Audio Treasury: All That Remains / Cruel & Unusual (Kay Scarpetta #3 #4) by Patricia Daniels Cornwell and Kate Burton",
                "Foodborne Parasites in the Food Supply Web: Occurrence and Control (Woodhead Publishing Series in Food Science, Technology and Nutrition)",
                "The Lost Tribe of Coney Island: Headhunters, Luna Park, and the Man Who Pulled Off the Spectacle of the Century by Claire Prentice",
                "Beyond the Beat: Musicians Building Community in Nashville by Daniel B. Cornfield",
                "Asthma-Free Naturally: Everything You Need to Know About Taking Control of Your Asthma--Featuring the Buteyko Breathing Method Suitable for Adults and Children by Patrick G. McKeown",
                "Blindness and Insight: Essays in the Rhetoric of Contemporary Criticism by Paul De Man and Wlad Godzich",
                "D:/CONTENT/Films/Serials/The_New_Pope",
                "The Punisher Vol. 6: Confederacy of Dunces by Garth Ennis",
                "The Stranger by Caroline B. Cooney and Albert Camus, and Stuart Gilbert",
                "The Sweet Potato Queens' Big-Ass Cookbook (and Financial Planner) by Jill Conner Browne",
                "The Contest (Everest #1) by Gordon Korman",
                "The Black Echo (Harry Bosch #1; Harry Bosch Universe #1) by Dick Hill and Michael Connelly",
                "The Lincoln Lawyer (A Lincoln Lawyer Novel) by Michael Connelly",
                "The Time Travelers: Volume One by Caroline B. Cooney",
                "The Annotated Waste Land with Eliot's Contemporary Prose by T.S. Eliot and Lawrence Rainey",
                "Be Confident Affirmations: Your Daily Affirmations to Increase Your Confidence Using the Power of the Law of Attraction by Stephens Hyang",
                "The Cuban Missile Crisis (Graphic Modern History: Cold War Conflicts) by Gary Jeffrey",
                "Brainstorms: Philosophical Essays on Mind and Psychology by Daniel C. Dennett",
                "Writing Rome: Textual Approaches to the City (Roman Literature and its Contexts) by Catharine Edwards",
                "Zest for Life: The Mediterranean Anti-Cancer Diet by Conner Middelmann-Whitney",
                "Downhill in Montana: Early Day Skiing in the Treasure State and Yellowstone National Park: A Pictoral History by Stan Cohen",
                "The Amen Corner by James Baldwin",
                "The Mind’s I: Fantasies and Reflections on Self and Soul by Douglas R. Hofstadter and Daniel C. Dennett",
                "Getting Past Your Past: Take Control of Your Life with Self-Help Techniques from EMDR Therapy by Francine Shapiro",
                "If the River Was Whiskey: Stories by T. Coraghessan Boyle",
                "The Face on the Milk Carton (Janie Johnson #1) by Caroline B. Cooney",
                "Bitter Is the New Black: Confessions of a Condescending Egomaniacal Self-Centered Smartass Or Why You Should Never Carry A Prada Bag to the Unemployment Office by Jen Lancaster",
                "D:/CONTENT/Films/Serials/The_Boys",
                "The Art of Deception: Controlling the Human Element of Security by Kevin D. Mitnick and William L. Simon, and Steve Wozniak",
                "Reinventing the Enemy's Language: Contemporary Native Women's Writings of North America",
                "OriginsThe term originated from the Bene Gesserit's prescient powers, and their inability to venture into a specific region of prescient knowledge. This region, though mysterious in nature, was known to be unattainable to females. Specifically, the spice melange allowed the Bene Gesserit to unlock genetic memory, but only on their maternal side. Female humans carry two X chromosomes, while males possess an X chromosome and a Y chromosome. A Kwisatz Haderach would be capable of accessing genetic memories stored within both chromosomes, while those memories were inaccessible to Bene Gesserit. Memories from male ancestors are still accessible to females with access to genetic memory (Alia is able to make contact with the ego memory of Vladimir Harkonnen for instance) but due to the lack of the Y chromosome, the memories were incomplete. Accessing the full paternal memories was impossible for the Bene Gesserit, and the very thought of trying was terrifying to them. Further, melange also exposed the Sisterhood (and others) to a limited form of prescience - thoughts, feelings, images into the near future, but no more.Bene Gesserit AttemptThe Bene Gesserit desire to uncover this knowledge and its associated powers, drove them to initiate a long-running genetic breeding program. This would yield a male with mental powers capable of bridging space and time and that he would be under the direct control of the Sisterhood. The Bene Gesserit knew that not only would their Kwisatz Haderach possess Other Memory on both the male and female side, but that he would be able to predict the future precisely. He would be Bene Gesserit Reverend Mother, Mentat, and Guild Navigator, all in one.After the emergence of this male - Paul Atreides - the term Kwisatz Haderach was also understood as meaning \"one who can be many places at once\", and became synonymous with Paul. In time, it also came to encompass his sister, Alia Atreides, and his son Leto Atreides II, since they all had similar abilities.The initial Bene Gesserit plan was to breed the daughter of Duke Leto Atreides to a Harkonnen male, which would produce the Kwisatz Haderach. This was to be the culmination of more than 10,000 years of careful breeding. This plan would have seen the end of the centuries-old feud between the Great Houses Atreides and Harkonnen, and placed a prescient, Bene Gesserit-controlled male on the Golden Lion Throne, the Kwisatz Haderach. However, because of her love for Duke Leto, the Lady Jessica disobeyed her fellow Bene Gesserit and gave birth to a son instead of a daughter, to give him an heir, Paul.It seemed fairly certain from early in Paul's life that he would in fact be the Kwisatz Haderach, since he showed an ability to see into the future. It was when he was fifteen years of age that the Bene Gesserit sent a Reverend Mother to test Paul's prescience and his training in the Bene Gesserit ways. This event appeared to be a significant catalyst for the events that would befall the universe for the next several thousand years. Paul's testing with the Gom Jabbar, as well as the Sisterhood's silent complicity in his father's death, proved to instill significant negativity in Paul against the Sisterhood. As a result, when he reached young adulthood, and ascended to the Golden Lion Throne, he vowed that he would never be under the control of the Sisterhood. Because of this, the Sisterhood lost control of their breeding program, their Kwisatz Haderach, and the possibility of placing a Bene Gesserit on the throne.Paul Leaves the Golden PathIndeed, it appeared that the majority of people did not realize that to know the future is to be trapped by it. Paul could see that he would have to lead humanity onto a drastic course in order for it to escape its own annihilation. As a result, he chose to escape the Golden Path by allowing himself to be blinded, so that he could walk into the desert and not be a burden on his tribe. The mantle of the Golden Path was subsequently taken up by Leto II.ImpactAs Kwisatz Haderach, both Paul and his son Leto would at times experience significant grief because they knew they were trapped by their own destinies. One of the few people who understood this was in fact one of Paul's enemies. It was through the failed Tleilaxu Kwisatz Haderach 'experiment' that the Tleilaxu Master Scytale could in fact sympathize with Paul and the decisions he had to make.When Leto II, the next Kwisatz Haderach, ascended to the throne, not only did he manage to avoid Bene Gesserit control, but he also took control of their breeding program. He then relegated them to a relatively insignificant role in the universe throughout the 3500 years of his reign.The ScatteringAfter the Famine Times prompted by Leto II after his death. The remaining power brokers in the Old Empire either forgot about the concept of the Kwisatz, or vehemently guarded against the rise of another of the super-beings. The Bene Gesserit would go so far as to terminate the life of humans that showed aspects of enhanced abilities. But as was shown with the \"wild\" Atreides line, the heightened human abilities continually showed themselves.After the The Scattering, both Miles Teg and his daughter Darwi Odrade, descendants of the Atreides, showed super-normal actions. Teg was able to move at lightning-fast speed and disabled his kidnappers on Gammu. Odrade had limited prescience that enabled her to know that the misguided Honored Matres and Bene Gesserit should merge to form one single organization.But it was the ghola of Duncan Idaho which caused the most concern for all involved. The conservative branch of the Sisterhood, led by Schwangyu, would go so far as to disobey the order of the Mother Superior Alma Mavis Taraza, and terminate the life of a young Duncan ghola -- even as she has sworn to be his protector from the Tleilaxu, who had allied themselves temporarily with the returning Honored Matres.Behind the scenesIt's likely that the term was borrowed by Frank Herbert from the Kabbala. The compilation of Jewish mysticism the core of which is entitled HaZohar (The Book of Splendor) ascribed to Rabbi Shimon bar-Yohai, ascribed by Jews to have been written in the 1st century C.E. Modern scholars ascribe it to a sephardic Jewish author around the 13th century. The term itself is Hebrew, \"K'fitzat ha-Derekh\", (קְפִיצַת הַדֶּרֶךְ) literally, \"The Leap of the Way,\" by means of which an initiate may travel some distance instantaneously, appearing to be in two or more places at once.",
                "The Wounded Healer: Ministry in Contemporary Society by Henri J.M. Nouwen",
                "A Contemporary Cuba Reader: The Revolution under Raúl Castro",
                "The Korean Mind: Understanding Contemporary Korean Culture by Boye Lafayette De Mente",
                "Home Cooking for Your Dog: 75 Holistic Recipes for a Healthier Dog by Christine Filardi",
                "Time Management from the Inside Out, Second Edition: The Foolproof System for Taking Control of Your Schedule -- and Your Life by Julie Morgenstern",
                "The Concrete Wave: The History of Skateboarding by Michael Brooke",
                "Beauty and the Contemporary Sublime by Jeremy Gilbert-Rolfe",
                "Architects of the Information Age (Computing and Connecting in the 21st Century)",
                "Historical Romances: The Prince and the Pauper / A Connecticut Yankee in King Arthur’s Court / Personal Recollections of Joan of Arc by Mark Twain and Susan K. Harris",
                "Facilitating the Socio-Economic Approach to Management: Results of the First SEAM Conference in North America (Research in Management Consulting)",
                "Lincoln and the Immigrant (Concise Lincoln Library) by Jason H. Silverman",
                "The Hip-Hop Church: Connecting with the Movement Shaping Our Culture by Efrem Smith and Phil Jackson",
                "The Worry Solution: Using Breakthrough Brain Science to Turn Stress and Anxiety into Confidence and Happiness by Martin Rossman M.D.",
                "Feeding the Young Athlete: Sports Nutrition Made Easy for Players, Parents, and Coaches by Cynthia Lair",
                "Obsessed: Breaking Free from the Things That Consume You by Hayley DiMarco",
                "The Anger Control Workbook by Matthew McKay",
                "Bodily Harm: The Breakthrough Healing Program For Self-Injurers by Karen Conterio",
                "Love 2.0: Finding Happiness and Health in Moments of Connection by Barbara L. Fredrickson Ph.D.",
                "Calm My Anxious Heart: A Woman's Guide to Finding Contentment (TH1NK Reference Collection) by Linda Dillow",
                "The Air-Conditioned Nightmare by Henry Miller",
                "A Gift from Bob: How a Street Cat Helped One Man Learn the Meaning of Christmas by James Bowen",
                "For the Love of a Dog: Understanding Emotion in You and Your Best Friend by Patricia B. McConnell",
                "Science and Football V: The Proceedings of the Fifth World Congress on Sports Science and Football (v. 5)",
                "Minnesota Muskie Fishing Map Guide (Sportsman's Connection)",
                "Sherlock Holmes: The Major Stories with Contemporary Critical Essays (Bedford Series in History & Culture) by Arthur Conan Doyle",
                "The Harry Bosch Novels Volume 2: The Last Coyote / Trunk Music / Angels Flight (Harry Bosch #4-6) by Michael Connelly",
                "Understanding Developments in Cyberspace Law: Leading Lawyers on Examining Privacy Issues, Addressing Security Concerns, and Responding to Recent IT Trends (Inside the Minds) by Multiple Authors",
                "Confessions of a Bad Teacher: The Shocking Truth from the Front Lines of American Public Education by John Owens",
                "The Pilgrimage: A Contemporary Quest for Ancient Wisdom by Paulo Coelho and Alan R. Clarke",
                "Free Culture: How Big Media Uses Technology and the Law to Lock Down Culture and Control Creativity by Lawrence Lessig",
                "Kenya: The Quest For Prosperity, Second Edition (Westview Profiles/Nations of Contemporary Africa) by Norman Miller",
                "Connecting Arduino: Programming And Networking With The Ethernet Shield by Bob Hammell",
                "If the River Was Whiskey by T. Coraghessan Boyle",
                "Stylepedia: A Guide to Graphic Design Mannerisms Quirks and Conceits by Steven Heller and Louise Fili",
                "A Woman in Berlin: Eight Weeks in the Conquered City: A Diary",
                "Soul of the Sword: An Illustrated History of Weaponry and Warfare from Prehistory to the Present by John Batchelor and Robert L. O'Connell",
                "From Enemy to Brother: The Revolution in Catholic Teaching on the Jews, 1933-1965 by John Connelly",
                "The Dragon Doesn't Live Here Anymore: Living Fully Loving Freely by Alan Cohen",
                "Is the Reformation Over?: An Evangelical Assessment of Contemporary Roman Catholicism by Mark A. Noll",
                "Gray's Anatomy: The Anatomical Basis of Clinical Practice, Expert Consult - Online and Print, 40e by Susan Standring PhD DSc",
                "Hajj Today: A Survey of the Contemporary Makkah Pilgrimage by David Edwin Long",
                "The Best American Mystery Stories 2003 by Otto Penzler and Michael Connelly",
                "No Price Too High: A Pentecostal Preacher Becomes Catholic - The Inspirational Story of Alex Jones as Told to Diane Hanson by Diane M. Hanson and Stephen K. Ray, and Alex C. Jones",
                "The Body Farm (Kay Scarpetta #5) by Patricia Daniels Cornwell",
                "My Story as told by Water: Confessions Druidic Rants Reflections Bird-watchings Fish-stalkings Visions Songs and Prayers Refracting Light from Living Rivers in the Age of the Industrial Dark by David James Duncan",
                "On the Bus: The Complete Guide to the Legendary Trip of Ken Kesey and the Merry Pranksters and the Birth of Counterculture by Paul Perry and Ken Babbs",
                "Complete Kickboxing: The Fighter's Ultimate Guide to Techniques, Concepts, and Strategy for Sparring and Competition by Martina Sprague",
                "The Fantastic Vampire: Studies in the Children of the Night: Selected Essays from the Eighteenth International Conference on the Fantastic in the Arts by James Craig Holte",
                "The Conquering Sword of Conan (Conan the Cimmerian #3) by Robert E. Howard and Patrice Louinet, and Gregory Manchess",
                "The Stickup Kids: Race, Drugs, Violence, and the American Dream by Randol Contreras",
                "McGraw-Hill's Dictionary of American Slang and Colloquial Expressions: The Most Up-to-Date Reference for the Nonstandard Usage, Popular Jargon, and Vulgarisms of Contempos (McGraw-Hill ESL References) by Richard Spears",
                "D:/CONTENT/Films/Serials/The_Mandalorian",
                "The Consolation of Philosophy: Boethius by Richard H. Green",
                "Freedom Evolves by Daniel C. Dennett",
                "Shakespeare's Kitchen: Renaissance Recipes for the Contemporary Cook by Francine Segan and Patrick O'Connell, and Tim Turner",
                "Pierre / Israel Potter / The Piazza Tales / The Confidence-Man / Uncollected Prose / Billy Budd by Harrison Hayford and Herman Melville",
                "The Band Played Dixie: Race and the Liberal Conscience at Ole Miss by Nadine Cohodas",
                "The Fatal Conceit: The Errors of Socialism (The Collected Works of F. A. Hayek) by F. A. Hayek",
                "The War of 1812: A Forgotten Conflict, Bicentennial Edition by Donald R Hickey",
                "Theme park state North Korea: It was considered under the blue sky of Pyongyang what freedom was. (Japanese Edition) by Go Arisugawa",
                "The Co-Parents' Handbook: Raising Well-Adjusted, Resilient, and Resourceful Kids in a Two-Home Family from Little Ones to Young Adults by Karen Bonnell",
                "The Counterlife by Philip Roth",
                "Where the Conflict Really Lies: Science, Religion, and Naturalism by Alvin Plantinga",
                "The Corrosion of Character: The Personal Consequences of Work in the New Capitalism by Richard Sennett",
                "Why Does He Do That?: Inside the Minds of Angry and Controlling Men by Lundy Bancroft",
                "After the Plague: and Other Stories by T. Coraghessan Boyle",
                "The Poet (Jack McEvoy #1; Harry Bosch Universe #5) by Buck Schirner and Michael Connelly",
                "The Consolation of Philosophy by Boethius and Victor Watts",
                "Charlie Wilson's War: The Extraordinary Story of How the Wildest Man in Congress and a Rogue CIA Agent Changed the History of our Times by George Crile",
                "Achieve PMP Exam Success, 5th Edition: A Concise Study Guide for the Busy Project Manager by Diane Altwies",
                "Vital Information and Review Questions for the NCE, CPCE and State Counseling Exams: Special 15th Anniversary Edition by Howard Rosenthal",
                "Book of the Dead (Kay Scarpetta #15) by Patricia Daniels Cornwell",
                "The Young Peacemaker: Teaching Students to Respond to Conflict in God's Way by Corlette Sande",
                "Modern Essentials a Contemporary Guide to the Therapeutic Use of Essential Oils (6th Edition) by Aroma Tools",
                "Healthy Cooking for IBS: 100 Delicious Recipes to Keep You Symptom-Free by Erica Jankovich and Sophie Braimbridge",
                "The Corner: A Year in the Life of an Inner-City Neighborhood by David Simon and Edward Burns",
                "The Norton Anthology of Modern and Contemporary Poetry by Richard Ellmann and Robert O'Clair, and Jahan Ramazani",
                "The Contest (Everest Trilogy) by Gordon Korman",
                "The Lost Lunar Baedeker: Poems of Mina Loy by Mina Loy and Roger L. Conover",
                "Contingency Hegemony Universality: Contemporary Dialogues on the Left by Slavoj Žižek and Judith Butler, and Ernesto Laclau",
                "Pragmatic Version Control: Using Subversion (The Pragmatic Starter Kit Series) by Mike Mason",
                "The Stories of John Cheever by Pelle Fritz-Crone and John Cheever",
                "Printing Our Way to Poverty: The Consequences of American Inflation by Mr. Pat McGeehan",
                "The Good Gut: Taking Control of Your Weight, Your Mood, and Your Long-term Health by Justin Sonnenburg",
                "The Virtue of Selfishness: A New Concept of Egoism by Ayn Rand and Nathaniel Branden",
                "Youth with Conduct Disorder: In Trouble with the World (Helping Youth with Mental, Physical, & Social Disabilities) by Kenneth McIntosh",
                "Ghosthunters and the Incredibly Revolting Ghost (Ghosthunters #1) by Cornelia Funke",
                "Health Care Ethics: Theological Foundations, Contemporary Issues, and Controversial Cases by Michael R. Panicola",
                "The Blonde on the Street Corner by David Goodis",
                "Beyond Consequences, Logic, and Control, Vol. 2 by Heather T. Forbes");

        expected = asList(
                "The Last Hours of Ancient Sunlight: The Fate of the World and What We Can Do Before It's Too Late by Neale Donald Walsch and Thom Hartmann, and Joseph Chilton Pearce");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_lasthrsnligh() {
        pattern = "lasthrsnligh";

        variants = asList(
                "Fear and Loathing in Las Vegas by Hunter S. Thompson and Ralph Steadman",
                "The Last Hours of Ancient Sunlight: The Fate of the World and What We Can Do Before It's Too Late by Neale Donald Walsch and Thom Hartmann, and Joseph Chilton Pearce");

        expected = asList(
                "The Last Hours of Ancient Sunlight: The Fate of the World and What We Can Do Before It's Too Late by Neale Donald Walsch and Thom Hartmann, and Joseph Chilton Pearce");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void npe() {
        pattern = "disrdarcv";

        variants = asList("$30 Film School: How to Write Direct Produce Shoot Edit Distribute Tour With and Sell Your Own No-Budget Digital Movie by Michael W. Dean");

        expected = asList();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_rrhgtphilpsy() {
        pattern = "rrhgtphilpsy";

        variants = asList("Philosophy of Right by S.W. Dyde and Georg Wilhelm Friedrich Hegel");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_prolins() {
        pattern = "prolins";

        variants = asList(
                "Mycoplasma Protocols (Methods in Molecular Biology)",
                "D:/SOUL/Programs/Links");

        expected = asList("D:/SOUL/Programs/Links");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_devenigs() {
        pattern = "devenigs";

        variants = asList("D:\\DEV\\2__LIB\\Engines");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_PriceAPICase_pricapi() {
        pattern = "pricapi";
        
        variants = asList(            
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/PriceCalculationAPI");
        
        expected = asList(
                "Projects/UkrPoshta/PriceCalculationAPI");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_PostAPICase_poshapi() {
        pattern = "poshapi";
        
        variants = asList(            
                "Projects/UkrPoshta/StatusTrackingAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/StatusTrackingAPI"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_TechBooksCase_() {
        pattern = "techbok";
        
        variants = asList(
                "Books/tech",
                "Books/Tech/Unsorted"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_WinampCase_() {
        pattern = "winan";
        
        variants = asList(
                "Winamp", 
                "Folder/Winamp.ext",                
                "Folder/Winamp_2.3.ext"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_PriceAPICase_ukrposporj() {
        pattern = "ukrposporj";
        
        variants = asList(            
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/PriceCalculationAPI");
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_MusicStoreCase_msustor() {
        pattern = "msustor";
        
        variants = asList(
                "Music/2__Store",
                "Music/2__Store/Therion"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_pstoapi() {
        pattern = "pstoapi";
        
        variants = asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta/UkrPostAPI");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_single_pstoapi() {
        pattern = "pstoapi";
        
        variants = asList(
                "Projects/UkrPoshta/UkrPostAPI");
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_single_potsapi() {
        pattern = "potsapi";
        
        variants = asList(
                "Projects/UkrPoshta/UkrPostAPI");
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_hiringCase_hring() {
        
        pattern = "hring";
        
        variants = asList(            
                "Current_Job/Hiring/CVs/Java_Junior/hr",
                "Job/Current/Hiring"
        );
        
        expected = asList(            
                "Job/Current/Hiring",
                "Current_Job/Hiring/CVs/Java_Junior/hr"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_reactJsCase_jsract() {
        
        pattern = "jsract";
        
        variants = asList(            
                "Java/Specifications/JPA_v.2.2_(JSR_318).pdf",
                "Projects/Diarsid/WebStorm/React.js"
        );
        
        expected = asList(            
                "Projects/Diarsid/WebStorm/React.js"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_fullNameCase_Current_Job() {
        
        pattern = "Current_Job";
        
        variants = asList(            
                "Current_Job/Hiring",
                "Current_Job"
        );
        
        expected = asList(            
                "Current_Job",
                "Current_Job/Hiring"
        );
        
        weightVariantsAndCheckMatching();
    }

    @Disabled("too much typo chars that resembles only by spelling")
    @Test
    public void test_kwizachoderah() {
        pattern = "kwizachoderah";

        variants = asList(
                "young adulthood kwisatz haderach he reached century ce modern taraza odrade scholars duncan idaho which"
        );

        expected = asList(
                "young adulthood kwisatz haderach he reached century ce modern taraza odrade scholars duncan idaho which"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_kwistzhadrch() {
        pattern = "kwistzhadrch";

        variants = asList(
                "young adulthood kwisatz haderach he reached century ce modern taraza odrade scholars duncan idaho which kwantum knowledge hadid as a result when he reached young adulthood"
        );

        expected = asList(
                "young adulthood kwisatz haderach he reached century ce modern taraza odrade scholars duncan idaho which kwantum knowledge hadid as a result when he reached young adulthood"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_kwistzhadrch_multiple() {
        pattern = "kwistzhadrch";

        variants = asList(
                "originsthe term originated from the bene gesserits prescient powers and their inability to venture into a specific region of prescient knowledge this region though mysterious in nature was known to be unattainable to females specifically the spice melange allowed the bene gesserit to unlock genetic memory but only on their maternal side female humans carry two x chromosomes while males possess an x chromosome and a y chromosome a kwisatz haderach would be capable of accessing genetic memories stored within both chromosomes while those memories were inaccessible to bene gesserit memories from male ancestors are still accessible to females with access to genetic memory alia is able to make contact with the ego memory of vladimir harkonnen for instance but due to the lack of the y chromosome the memories were incomplete accessing the full paternal memories was impossible for the bene gesserit and the very thought of trying was terrifying to them further melange also exposed the sisterhood and others to a limited form of prescience   thoughts feelings images into the near future but no morebene gesserit attemptthe bene gesserit desire to uncover this knowledge and its associated powers drove them to initiate a long running genetic breeding program this would yield a male with mental powers capable of bridging space and time and that he would be under the direct control of the sisterhood the bene gesserit knew that not only would their kwisatz haderach possess other memory on both the male and female side but that he would be able to predict the future precisely he would be bene gesserit reverend mother mentat and guild navigator all in oneafter the emergence of this male   paul atreides   the term kwisatz haderach was also understood as meaning one who can be many places at once and became synonymous with paul in time it also came to encompass his sister alia atreides and his son leto atreides ii since they all had similar abilitiesthe initial bene gesserit plan was to breed the daughter of duke leto atreides to a harkonnen male which would produce the kwisatz haderach this was to be the culmination of more than 10000 years of careful breeding this plan would have seen the end of the centuries old feud between the great houses atreides and harkonnen and placed a prescient bene gesserit controlled male on the golden lion throne the kwisatz haderach however because of her love for duke leto the lady jessica disobeyed her fellow bene gesserit and gave birth to a son instead of a daughter to give him an heir paulit seemed fairly certain from early in pauls life that he would in fact be the kwisatz haderach since he showed an ability to see into the future it was when he was fifteen years of age that the bene gesserit sent a reverend mother to test pauls prescience and his training in the bene gesserit ways this event appeared to be a significant catalyst for the events that would befall the universe for the next several thousand years pauls testing with the gom jabbar as well as the sisterhoods silent complicity in his fathers death proved to instill significant negativity in paul against the sisterhood as a result when he reached young adulthood and ascended to the golden lion throne he vowed that he would never be under the control of the sisterhood because of this the sisterhood lost control of their breeding program their kwisatz haderach and the possibility of placing a bene gesserit on the thronepaul leaves the golden pathindeed it appeared that the majority of people did not realize that to know the future is to be trapped by it paul could see that he would have to lead humanity onto a drastic course in order for it to escape its own annihilation as a result he chose to escape the golden path by allowing himself to be blinded so that he could walk into the desert and not be a burden on his tribe the mantle of the golden path was subsequently taken up by leto iiimpactas kwisatz haderach both paul and his son leto would at times experience significant grief because they knew they were trapped by their own destinies one of the few people who understood this was in fact one of pauls enemies it was through the failed tleilaxu kwisatz haderach experiment that the tleilaxu master scytale could in fact sympathize with paul and the decisions he had to makewhen leto ii the next kwisatz haderach ascended to the throne not only did he manage to avoid bene gesserit control but he also took control of their breeding program he then relegated them to a relatively insignificant role in the universe throughout the 3500 years of his reignthe scatteringafter the famine times prompted by leto ii after his death the remaining power brokers in the old empire either forgot about the concept of the kwisatz or vehemently guarded against the rise of another of the super beings the bene gesserit would go so far as to terminate the life of humans that showed aspects of enhanced abilities but as was shown with the wild atreides line the heightened human abilities continually showed themselvesafter the the scattering both miles teg and his daughter darwi odrade descendants of the atreides showed super normal actions teg was able to move at lightning fast speed and disabled his kidnappers on gammu odrade had limited prescience that enabled her to know that the misguided honored matres and bene gesserit should merge to form one single organizationbut it was the ghola of duncan idaho which caused the most concern for all involved the conservative branch of the sisterhood led by schwangyu would go so far as to disobey the order of the mother superior alma mavis taraza and terminate the life of a young duncan ghola   even as she has sworn to be his protector from the tleilaxu who had allied themselves temporarily with the returning honored matresbehind the scenesits likely that the term was borrowed by frank herbert from the kabbala the compilation of jewish mysticism the core of which is entitled hazohar the book of splendor ascribed to rabbi shimon bar yohai ascribed by jews to have been written in the 1st century ce modern scholars ascribe it to a sephardic jewish author around the 13th century the term itself is hebrew kfitzat ha derekh קְפִיצַת הַדֶּרֶךְ literally the leap of the way by means of which an initiate may travel some distance instantaneously appearing to be in two or more places at once"
        );

        expected = asList(
                "originsthe term originated from the bene gesserits prescient powers and their inability to venture into a specific region of prescient knowledge this region though mysterious in nature was known to be unattainable to females specifically the spice melange allowed the bene gesserit to unlock genetic memory but only on their maternal side female humans carry two x chromosomes while males possess an x chromosome and a y chromosome a kwisatz haderach would be capable of accessing genetic memories stored within both chromosomes while those memories were inaccessible to bene gesserit memories from male ancestors are still accessible to females with access to genetic memory alia is able to make contact with the ego memory of vladimir harkonnen for instance but due to the lack of the y chromosome the memories were incomplete accessing the full paternal memories was impossible for the bene gesserit and the very thought of trying was terrifying to them further melange also exposed the sisterhood and others to a limited form of prescience   thoughts feelings images into the near future but no morebene gesserit attemptthe bene gesserit desire to uncover this knowledge and its associated powers drove them to initiate a long running genetic breeding program this would yield a male with mental powers capable of bridging space and time and that he would be under the direct control of the sisterhood the bene gesserit knew that not only would their kwisatz haderach possess other memory on both the male and female side but that he would be able to predict the future precisely he would be bene gesserit reverend mother mentat and guild navigator all in oneafter the emergence of this male   paul atreides   the term kwisatz haderach was also understood as meaning one who can be many places at once and became synonymous with paul in time it also came to encompass his sister alia atreides and his son leto atreides ii since they all had similar abilitiesthe initial bene gesserit plan was to breed the daughter of duke leto atreides to a harkonnen male which would produce the kwisatz haderach this was to be the culmination of more than 10000 years of careful breeding this plan would have seen the end of the centuries old feud between the great houses atreides and harkonnen and placed a prescient bene gesserit controlled male on the golden lion throne the kwisatz haderach however because of her love for duke leto the lady jessica disobeyed her fellow bene gesserit and gave birth to a son instead of a daughter to give him an heir paulit seemed fairly certain from early in pauls life that he would in fact be the kwisatz haderach since he showed an ability to see into the future it was when he was fifteen years of age that the bene gesserit sent a reverend mother to test pauls prescience and his training in the bene gesserit ways this event appeared to be a significant catalyst for the events that would befall the universe for the next several thousand years pauls testing with the gom jabbar as well as the sisterhoods silent complicity in his fathers death proved to instill significant negativity in paul against the sisterhood as a result when he reached young adulthood and ascended to the golden lion throne he vowed that he would never be under the control of the sisterhood because of this the sisterhood lost control of their breeding program their kwisatz haderach and the possibility of placing a bene gesserit on the thronepaul leaves the golden pathindeed it appeared that the majority of people did not realize that to know the future is to be trapped by it paul could see that he would have to lead humanity onto a drastic course in order for it to escape its own annihilation as a result he chose to escape the golden path by allowing himself to be blinded so that he could walk into the desert and not be a burden on his tribe the mantle of the golden path was subsequently taken up by leto iiimpactas kwisatz haderach both paul and his son leto would at times experience significant grief because they knew they were trapped by their own destinies one of the few people who understood this was in fact one of pauls enemies it was through the failed tleilaxu kwisatz haderach experiment that the tleilaxu master scytale could in fact sympathize with paul and the decisions he had to makewhen leto ii the next kwisatz haderach ascended to the throne not only did he manage to avoid bene gesserit control but he also took control of their breeding program he then relegated them to a relatively insignificant role in the universe throughout the 3500 years of his reignthe scatteringafter the famine times prompted by leto ii after his death the remaining power brokers in the old empire either forgot about the concept of the kwisatz or vehemently guarded against the rise of another of the super beings the bene gesserit would go so far as to terminate the life of humans that showed aspects of enhanced abilities but as was shown with the wild atreides line the heightened human abilities continually showed themselvesafter the the scattering both miles teg and his daughter darwi odrade descendants of the atreides showed super normal actions teg was able to move at lightning fast speed and disabled his kidnappers on gammu odrade had limited prescience that enabled her to know that the misguided honored matres and bene gesserit should merge to form one single organizationbut it was the ghola of duncan idaho which caused the most concern for all involved the conservative branch of the sisterhood led by schwangyu would go so far as to disobey the order of the mother superior alma mavis taraza and terminate the life of a young duncan ghola   even as she has sworn to be his protector from the tleilaxu who had allied themselves temporarily with the returning honored matresbehind the scenesits likely that the term was borrowed by frank herbert from the kabbala the compilation of jewish mysticism the core of which is entitled hazohar the book of splendor ascribed to rabbi shimon bar yohai ascribed by jews to have been written in the 1st century ce modern scholars ascribe it to a sephardic jewish author around the 13th century the term itself is hebrew kfitzat ha derekh קְפִיצַת הַדֶּרֶךְ literally the leap of the way by means of which an initiate may travel some distance instantaneously appearing to be in two or more places at once"
        );

        weightVariantsAndCheckMatching();
    }

    @Disabled
    @Test
    public void test_kwistchadrech_multiple() {
        pattern = "kwistchadrech";

        variants = asList(
                "originsthe term originated from the bene gesserits prescient powers and their inability to venture into a specific region of prescient knowledge this region though mysterious in nature was known to be unattainable to females specifically the spice melange allowed the bene gesserit to unlock genetic memory but only on their maternal side female humans carry two x chromosomes while males possess an x chromosome and a y chromosome a kwisatz haderach would be capable of accessing genetic memories stored within both chromosomes while those memories were inaccessible to bene gesserit memories from male ancestors are still accessible to females with access to genetic memory alia is able to make contact with the ego memory of vladimir harkonnen for instance but due to the lack of the y chromosome the memories were incomplete accessing the full paternal memories was impossible for the bene gesserit and the very thought of trying was terrifying to them further melange also exposed the sisterhood and others to a limited form of prescience   thoughts feelings images into the near future but no morebene gesserit attemptthe bene gesserit desire to uncover this knowledge and its associated powers drove them to initiate a long running genetic breeding program this would yield a male with mental powers capable of bridging space and time and that he would be under the direct control of the sisterhood the bene gesserit knew that not only would their kwisatz haderach possess other memory on both the male and female side but that he would be able to predict the future precisely he would be bene gesserit reverend mother mentat and guild navigator all in oneafter the emergence of this male   paul atreides   the term kwisatz haderach was also understood as meaning one who can be many places at once and became synonymous with paul in time it also came to encompass his sister alia atreides and his son leto atreides ii since they all had similar abilitiesthe initial bene gesserit plan was to breed the daughter of duke leto atreides to a harkonnen male which would produce the kwisatz haderach this was to be the culmination of more than 10000 years of careful breeding this plan would have seen the end of the centuries old feud between the great houses atreides and harkonnen and placed a prescient bene gesserit controlled male on the golden lion throne the kwisatz haderach however because of her love for duke leto the lady jessica disobeyed her fellow bene gesserit and gave birth to a son instead of a daughter to give him an heir paulit seemed fairly certain from early in pauls life that he would in fact be the kwisatz haderach since he showed an ability to see into the future it was when he was fifteen years of age that the bene gesserit sent a reverend mother to test pauls prescience and his training in the bene gesserit ways this event appeared to be a significant catalyst for the events that would befall the universe for the next several thousand years pauls testing with the gom jabbar as well as the sisterhoods silent complicity in his fathers death proved to instill significant negativity in paul against the sisterhood as a result when he reached young adulthood and ascended to the golden lion throne he vowed that he would never be under the control of the sisterhood because of this the sisterhood lost control of their breeding program their kwisatz haderach and the possibility of placing a bene gesserit on the thronepaul leaves the golden pathindeed it appeared that the majority of people did not realize that to know the future is to be trapped by it paul could see that he would have to lead humanity onto a drastic course in order for it to escape its own annihilation as a result he chose to escape the golden path by allowing himself to be blinded so that he could walk into the desert and not be a burden on his tribe the mantle of the golden path was subsequently taken up by leto iiimpactas kwisatz haderach both paul and his son leto would at times experience significant grief because they knew they were trapped by their own destinies one of the few people who understood this was in fact one of pauls enemies it was through the failed tleilaxu kwisatz haderach experiment that the tleilaxu master scytale could in fact sympathize with paul and the decisions he had to makewhen leto ii the next kwisatz haderach ascended to the throne not only did he manage to avoid bene gesserit control but he also took control of their breeding program he then relegated them to a relatively insignificant role in the universe throughout the 3500 years of his reignthe scatteringafter the famine times prompted by leto ii after his death the remaining power brokers in the old empire either forgot about the concept of the kwisatz or vehemently guarded against the rise of another of the super beings the bene gesserit would go so far as to terminate the life of humans that showed aspects of enhanced abilities but as was shown with the wild atreides line the heightened human abilities continually showed themselvesafter the the scattering both miles teg and his daughter darwi odrade descendants of the atreides showed super normal actions teg was able to move at lightning fast speed and disabled his kidnappers on gammu odrade had limited prescience that enabled her to know that the misguided honored matres and bene gesserit should merge to form one single organizationbut it was the ghola of duncan idaho which caused the most concern for all involved the conservative branch of the sisterhood led by schwangyu would go so far as to disobey the order of the mother superior alma mavis taraza and terminate the life of a young duncan ghola   even as she has sworn to be his protector from the tleilaxu who had allied themselves temporarily with the returning honored matresbehind the scenesits likely that the term was borrowed by frank herbert from the kabbala the compilation of jewish mysticism the core of which is entitled hazohar the book of splendor ascribed to rabbi shimon bar yohai ascribed by jews to have been written in the 1st century ce modern scholars ascribe it to a sephardic jewish author around the 13th century the term itself is hebrew kfitzat ha derekh קְפִיצַת הַדֶּרֶךְ literally the leap of the way by means of which an initiate may travel some distance instantaneously appearing to be in two or more places at once"
        );

        expected = asList(
                "originsthe term originated from the bene gesserits prescient powers and their inability to venture into a specific region of prescient knowledge this region though mysterious in nature was known to be unattainable to females specifically the spice melange allowed the bene gesserit to unlock genetic memory but only on their maternal side female humans carry two x chromosomes while males possess an x chromosome and a y chromosome a kwisatz haderach would be capable of accessing genetic memories stored within both chromosomes while those memories were inaccessible to bene gesserit memories from male ancestors are still accessible to females with access to genetic memory alia is able to make contact with the ego memory of vladimir harkonnen for instance but due to the lack of the y chromosome the memories were incomplete accessing the full paternal memories was impossible for the bene gesserit and the very thought of trying was terrifying to them further melange also exposed the sisterhood and others to a limited form of prescience   thoughts feelings images into the near future but no morebene gesserit attemptthe bene gesserit desire to uncover this knowledge and its associated powers drove them to initiate a long running genetic breeding program this would yield a male with mental powers capable of bridging space and time and that he would be under the direct control of the sisterhood the bene gesserit knew that not only would their kwisatz haderach possess other memory on both the male and female side but that he would be able to predict the future precisely he would be bene gesserit reverend mother mentat and guild navigator all in oneafter the emergence of this male   paul atreides   the term kwisatz haderach was also understood as meaning one who can be many places at once and became synonymous with paul in time it also came to encompass his sister alia atreides and his son leto atreides ii since they all had similar abilitiesthe initial bene gesserit plan was to breed the daughter of duke leto atreides to a harkonnen male which would produce the kwisatz haderach this was to be the culmination of more than 10000 years of careful breeding this plan would have seen the end of the centuries old feud between the great houses atreides and harkonnen and placed a prescient bene gesserit controlled male on the golden lion throne the kwisatz haderach however because of her love for duke leto the lady jessica disobeyed her fellow bene gesserit and gave birth to a son instead of a daughter to give him an heir paulit seemed fairly certain from early in pauls life that he would in fact be the kwisatz haderach since he showed an ability to see into the future it was when he was fifteen years of age that the bene gesserit sent a reverend mother to test pauls prescience and his training in the bene gesserit ways this event appeared to be a significant catalyst for the events that would befall the universe for the next several thousand years pauls testing with the gom jabbar as well as the sisterhoods silent complicity in his fathers death proved to instill significant negativity in paul against the sisterhood as a result when he reached young adulthood and ascended to the golden lion throne he vowed that he would never be under the control of the sisterhood because of this the sisterhood lost control of their breeding program their kwisatz haderach and the possibility of placing a bene gesserit on the thronepaul leaves the golden pathindeed it appeared that the majority of people did not realize that to know the future is to be trapped by it paul could see that he would have to lead humanity onto a drastic course in order for it to escape its own annihilation as a result he chose to escape the golden path by allowing himself to be blinded so that he could walk into the desert and not be a burden on his tribe the mantle of the golden path was subsequently taken up by leto iiimpactas kwisatz haderach both paul and his son leto would at times experience significant grief because they knew they were trapped by their own destinies one of the few people who understood this was in fact one of pauls enemies it was through the failed tleilaxu kwisatz haderach experiment that the tleilaxu master scytale could in fact sympathize with paul and the decisions he had to makewhen leto ii the next kwisatz haderach ascended to the throne not only did he manage to avoid bene gesserit control but he also took control of their breeding program he then relegated them to a relatively insignificant role in the universe throughout the 3500 years of his reignthe scatteringafter the famine times prompted by leto ii after his death the remaining power brokers in the old empire either forgot about the concept of the kwisatz or vehemently guarded against the rise of another of the super beings the bene gesserit would go so far as to terminate the life of humans that showed aspects of enhanced abilities but as was shown with the wild atreides line the heightened human abilities continually showed themselvesafter the the scattering both miles teg and his daughter darwi odrade descendants of the atreides showed super normal actions teg was able to move at lightning fast speed and disabled his kidnappers on gammu odrade had limited prescience that enabled her to know that the misguided honored matres and bene gesserit should merge to form one single organizationbut it was the ghola of duncan idaho which caused the most concern for all involved the conservative branch of the sisterhood led by schwangyu would go so far as to disobey the order of the mother superior alma mavis taraza and terminate the life of a young duncan ghola   even as she has sworn to be his protector from the tleilaxu who had allied themselves temporarily with the returning honored matresbehind the scenesits likely that the term was borrowed by frank herbert from the kabbala the compilation of jewish mysticism the core of which is entitled hazohar the book of splendor ascribed to rabbi shimon bar yohai ascribed by jews to have been written in the 1st century ce modern scholars ascribe it to a sephardic jewish author around the 13th century the term itself is hebrew kfitzat ha derekh קְפִיצַת הַדֶּרֶךְ literally the leap of the way by means of which an initiate may travel some distance instantaneously appearing to be in two or more places at once"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_kwistchadrech() {
        pattern = "kwistchadrech";

        variants = asList(
                "young adulthood kwisatz haderach he reached century ce modern taraza odrade scholars duncan idaho which"
        );

        expected = asList(
                "young adulthood kwisatz haderach he reached century ce modern taraza odrade scholars duncan idaho which"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projscs() {
        pattern = "projscs";

        variants = asList(
                "Raspberry Pi Projects for the Evil Genius by Donald Norris",
                "D:/DEV/1__Projects/Diarsid/src",
                "D:/DEV/1__Projects/Diarsid"
        );

        expected = asList(
                "D:/DEV/1__Projects/Diarsid",
                "D:/DEV/1__Projects/Diarsid/src",
                "Raspberry Pi Projects for the Evil Genius by Donald Norris"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_tools_1() {
        pattern = "tolos";

        variants = asList(
                "The Dictionary of Corporate Bullshit: An A to Z Lexicon of Empty Enraging and Just Plain Stupid Office Talk by Lois Beckwith",
                "D:/DEV/3__Tools/Servers/Message_Servers",
                "D:/DEV/3__Tools",
                "Star Trek: Logs One and Two (Star Trek: Log #1-2) by Alan Dean Foster",
                "Eating for Life: Your Guide to Great Health Fat Loss and Increased Energy! by Bill Phillips",
                "The Hunger Games Companion: The Unauthorized Guide to the Series by Lois H. Gresh",
                "Losers: The Road to Everyplace But the White House by Michael Lewis",
                "A Calendar of Wisdom: Daily Thoughts to Nourish the Soul by Peter Sekirin and Leo Tolstoy",
                "The Book of Lost Tales Part One (The History of Middle-Earth #1) by J.R.R. Tolkien and Christopher Tolkien"
        );

        expected = asList(
                "D:/DEV/3__Tools",
                "D:/DEV/3__Tools/Servers/Message_Servers",
                "Eating for Life: Your Guide to Great Health Fat Loss and Increased Energy! by Bill Phillips",
                "Losers: The Road to Everyplace But the White House by Michael Lewis",
                "The Hunger Games Companion: The Unauthorized Guide to the Series by Lois H. Gresh",
                "The Book of Lost Tales Part One (The History of Middle-Earth #1) by J.R.R. Tolkien and Christopher Tolkien",
                "Star Trek: Logs One and Two (Star Trek: Log #1-2) by Alan Dean Foster",
                "The Dictionary of Corporate Bullshit: An A to Z Lexicon of Empty Enraging and Just Plain Stupid Office Talk by Lois Beckwith"
        );

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_tolservrs() {
        pattern = "tolservrs";

        variants = asList(
                "D:/DEV/3__Tools/Servers"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_3toolssevrirtl() {
        pattern = "3toolssevrirtl";

        variants = asList(
                "D:/DEV/3__Tools/Servers/Virtualization_Servers"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_tolsvirtl_1() {
        pattern = "tolsvirtl";

        variants = asList(
                "The Lady in the Lake The Little Sister The Long Goodbye Playback (Everyman's Library) by Tom Hiney and Raymond Chandler",
                "The Future of God: A Practical Approach to Spirituality for Our Times by Deepak Chopra",
                "The Diary of Virginia Woolf Volume Two: 1920-1924 by Virginia Woolf and Andrew McNeillie, and Anne Olivier Bell",
                "The Ice-Shirt (Seven Dreams #1) by William T. Vollmann",
                "There's a Spiritual Solution to Every Problem by Dr. Wayne W. Dyer",
                "Toys Go Out: Being the Adventures of a Knowledgeable Stingray a Toughy Little Buffalo and Someone Called Plastic (Toys #1) by Emily Jenkins and Paul O. Zelinsky",
                "Java: An Introduction to Problem Solving and Programming by Walter J. Savitch",
                "D:/DEV/3__Tools/Servers/Virtualization_Servers"
        );

        expected = asList(
                "D:/DEV/3__Tools/Servers/Virtualization_Servers");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_drklalver() {
        pattern = "drklalver";

        variants = asList(
                "Dracula's Lover (Erotic Monsters Series) (Volume 2) by J.G. Newton",
                "H. P. Lovecraft's Dreamlands (Call of Cthulhu RPG) by Chris Williams and Sandy Petersen",
                "Student Friendly Quantum Field Theory by Robert D. Klauber"
        );

        expected = asList("Dracula's Lover (Erotic Monsters Series) (Volume 2) by J.G. Newton");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_drclalver() {
        pattern = "drclalver";

        variants = asList(
                "Dracula's Lover (Erotic Monsters Series) (Volume 2) by J.G. Newton",
                "H. P. Lovecraft's Dreamlands (Call of Cthulhu RPG) by Chris Williams and Sandy Petersen",
                "Student Friendly Quantum Field Theory by Robert D. Klauber"
        );

        expected = asList("Dracula's Lover (Erotic Monsters Series) (Volume 2) by J.G. Newton");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_romeries() {
        pattern = "romeries";

        variants = asList(
                "When Genius Failed: The Rise And Fall Of Long Term Capital Management by Roger Lowenstein",
                "Bedlam's Edge (Bedlam's Bard #8) by Mercedes Lackey and Rosemary Edghill",
                "The Golden Key by Jennifer Roberson and Melanie Rawn, and Kate Elliott",
                "Accidental Empires by Robert X. Cringely",
                "How the Irish Saved Civilization: The Untold Story of Ireland's Heroic Role from the Fall of Rome to the Rise of Medieval Europe by Thomas Cahill",
                "Britain After Rome: The Fall and Rise, 400 to 1070 by Robin Fleming"
        );

        expected = asList(
                "Britain After Rome: The Fall and Rise, 400 to 1070 by Robin Fleming",
                "How the Irish Saved Civilization: The Untold Story of Ireland's Heroic Role from the Fall of Rome to the Rise of Medieval Europe by Thomas Cahill");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_srvsmsg() {
        pattern = "srvsmsg";

        variants = asList(
                "D:/DEV/3__Tools/Servers/Message_Servers"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_srvsmesg() {
        pattern = "srvsmesg";

        variants = asList(
                "D:/DEV/3__Tools/Servers/Message_Servers"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_uposhtarch() {
        pattern = "uposhtarch";

        variants = asList(
                "D:/DEV/1__Projects/X__Archive/UkrPoshta"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_proupsth() {
        pattern = "proupsth";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationService",
                "D:/DEV/1__Projects/UkrPoshta",
                "D:/DEV/1__Projects/UkrPoshta/StatusNotificationService"
        );

        expected = asList(
                "D:/DEV/1__Projects/UkrPoshta",
                "D:/DEV/1__Projects/UkrPoshta/StatusNotificationService",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationService");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projupsth_2() {
        pattern = "projupsth";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta",
                "D:/DEV/1__Projects/UkrPoshta/StatusNotificationService",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationService"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projupsth_1() {
        pattern = "projupsth";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta",
                "The Judicial Process: Law, Courts, and Judicial Politics by Christopher P. Banks"
        );

        expected = asList(
                "D:/DEV/1__Projects/UkrPoshta");

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projsupsth_1() {
        pattern = "projsupsth";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projsupsth() {
        pattern = "projsupsth";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta",
                "D:/DEV/1__Projects/UkrPoshta/StatusNotificationService",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationService"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_upsthprojs() {
        pattern = "upsthprojs";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta",
                "D:/DEV/1__Projects/UkrPoshta/StatusNotificationService",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationService"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_passnjss() {
        pattern = "passnjss";

        variants = asList(
                "The Passion of Jesus Christ by John Piper"
        );

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }



    @Test
    public void test_virtlservs_virtual_servers() {
        pattern = "virtlservs";

        variants = asList(
                "Resurrection After Rape: A guide to transforming from victim to survivor by Matt Atkinson",
                "D:/DEV/3__Tools/Servers/Virtualization_Servers",
                "On Vital Reserves by Stephen Vicchio and James William Martin"
        );

        expected = asList(
                "D:/DEV/3__Tools/Servers/Virtualization_Servers",
                "On Vital Reserves by Stephen Vicchio and James William Martin"
                );

        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_mathCase_math() {
        
        pattern = "math";
        
        variants = asList( 
                "math",
                "math/autocad/xxx",
                "xxx/autocad/Math_other"
        );
        
        expected = asList(
                "math",
                "xxx/autocad/Math_other",
                "math/autocad/xxx"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_mathCase_math_longerCase() {
        
        pattern = "math";
        
        variants = asList( 
                "math/autocad",
                "Books/Tech/Math_&_CompScience"
        );
        
        expected = asList(
                "Books/Tech/Math_&_CompScience",
                "math/autocad"
        );
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_servs_stress() {

        pattern = "servs";

        variants = asList(
                "stress"
        );

        expected = asList();

        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_JavaPathBinCase_jbin() {
        pattern = "jbin";
        
        variants = asList(            
                "Current_Job/domain",
                "Current_Job/domain/tmm",
                "Current_Job/hiring",
                "Engines/java/path/JAVA_HOME/bin");
        
        expected = asList(
                "Engines/java/path/JAVA_HOME/bin");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_JavaPathBinCase_jbin_2() {
        pattern = "jbin";

        variants = asList(
                "Current_Job/domain");

        expected = asList();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_hofmaninctant() {
        pattern = "hofmaninctant";

        variants = asList(
                "Incantation by Alice Hoffman");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_rhgtphilpsy() {
        pattern = "rhgtphilpsy";

        variants = asList(
                "Philosophy of Right by S.W. Dyde and Georg Wilhelm Friedrich Hegel");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    @Disabled("don't know what to do wuth: mAn -> mEn")
    public void test_godmanwar() {
        pattern = "godmanwar";

        variants = asList(
                "The Wars of Gods and Men: Book III of the Earth Chronicles (The Earth Chronicles) by Zecharia Sitchin",
                "On Killing: The Psychological Cost of Learning to Kill in War and Society by Dave Grossman");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_godmenwar() {
        pattern = "godmenwar";

        variants = asList(
                "The Wars of Gods and Men: Book III of the Earth Chronicles (The Earth Chronicles) by Zecharia Sitchin");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_godmnwar() {
        pattern = "godmnwar";

        variants = asList(
                "The Wars of Gods and Men: Book III of the Earth Chronicles (The Earth Chronicles) by Zecharia Sitchin");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_gdmenwar() {
        pattern = "gdmenwar";

        variants = asList(
                "The Wars of Gods and Men: Book III of the Earth Chronicles (The Earth Chronicles) by Zecharia Sitchin",
                "On Killing: The Psychological Cost of Learning to Kill in War and Society by Dave Grossman");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_godmenwr() {
        pattern = "godmenwr";

        variants = asList(
                "The Wars of Gods and Men: Book III of the Earth Chronicles (The Earth Chronicles) by Zecharia Sitchin");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_pirctstokn() {
        pattern = "pirctstokn";

        variants = asList(
                "Pictures by J.R.R. Tolkien by J.R.R. Tolkien and Christopher Tolkien");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_crishphrtolkinjrr() {
        pattern = "crishphrtolkinjrr";

        variants = asList(
                "Pictures by J.R.R. Tolkien by J.R.R. Tolkien and Christopher Tolkien");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_brontstreflc() {
        pattern = "brontstreflc";

        variants = asList(
                "Bully for Brontosaurus: Reflections in Natural History by Stephen Jay Gould",
                "When the Siren Wailed by Judith Gwyn Brown and Noel Streatfeild");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_projukrpostats() {
        pattern = "projukrpostats";

        variants = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusTracking",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationService");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_progsloclscl() {
        pattern = "progsloclscl";

        variants = asList(
                "D:/SOUL/Programs/Locally/Social");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_progsloclscl_other() {
        pattern = "progsloclscl";

        variants = asList(
                "freedom of contract and paternalism: prospects and limits of an economic approach (perspectives from social economics) by péter cserne"
        );

        expected = asList();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_solmnmies() {
        pattern = "solmnmies";

        variants = asList(
                "King Solomon's Mines (Allan Quatermain #1) by H. Rider Haggard");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_synthetic_1() {
        pattern = "abc123";
        
        variants = asList(            
                "xy/ABC_z123er",
                "ABC/123",
                "qwCABgfg132",
                "xcdfABdC_fg123fdf23hj12");
        
        expected = asList(
                "ABC/123",
                "xy/ABC_z123er",
                "xcdfABdC_fg123fdf23hj12");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_synthetic_2() {
        pattern = "abc123";
        
        variants = asList(            
                "xy/ABC_z123er/Ab",
                "xy/ABC_123er");
        
        expected = asList(       
                "xy/ABC_123er", 
                "xy/ABC_z123er/Ab"
                );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_synthetic_3() {
        pattern = "abcXYZ";
        
        variants = asList(            
                "ababbaccaABCabbac_xyyxzyyxzXYZzx",
                "ababbaccaACBabbac_xyyxzyyxzXYZzx");
        
        expected = asList(
                "ababbaccaABCabbac_xyyxzyyxzXYZzx",
                "ababbaccaACBabbac_xyyxzyyxzXYZzx");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_synthetic_placingImportance() {
        pattern = "abcXYZ";
        
        variants = asList(            
                "some/path/ending/ABC/with_XYZ_end",
                "some/ABC/begin/XYZ/no_ending_with");
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_synthetic() {
        pattern = "abcXYZ";
        
        variants = asList( 
                "ABC_XYZ",        
                "ABC_XYZ_acb",
                "zx_ABC_XYZ");
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_synthetic_distanceBetweenClustersIsMoreImportantThanPlacing() {
        pattern = "abcXYZ";
        
        variants = asList( 
                "abbac_ABC_ba_XYZ_bacxy", 
                "caba/abbac_ABC_ba_XYZ_bacxyyxzyyxz_zx",
                "a_xyyxzyyxz_zx/ABC_bbacbacacaba_XYZ_b", 
                "ABC_ba_XYZ_baccaba/abbac_xyyxzyyxz_zx",
                "ABC_bbacbacacaba_XYZ_b/a_xyyxzyyxz_zx"               
                );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }

    @Disabled
    @Test
    public void test_synthetic_4() {
        pattern = "abcXYZ";
        
        variants = asList(                
                "ABCXYZ",
                "ABCXYZ_acba",
                "zx_ABCXYZ_acba",
                "ABCXYZ_acba/abac_xyyxz_zx",
                "ABCXYZ_ababbacca/abbac_xyyxzyyxz_zx",
                "zx_ABCXYZ_acba/abac_xyyxz_zx",
                "zx_ABCXYZ_ababbacca/abbac_xyyxzyyxz_zx",
                "ABCXYZacba",
                "axABCXYZ_abaca/ab_xyyxz_zx",
                "axABCXYZacba",
                "axABCXYZ_ababbacca/abbac_xyyxzyyxz_zx",
                "axABCXYZacba_ab/ab",
                "ABC_XYZ",
                "ABC_XYZ_acb",
                "zx_ABC_XYZ",
                "abbac_xyyxzyyxz_zx/ABC_XYZ_bac_ba_caba",
                "abbac_xyyxzyyxz_zx/caba_bac_ba_ABC_XYZ",
                "ABC_XYZ_ababbacca/abbac_xyyxzyyxz_zx",
                "abbac_xy/cab_bac_ba_ABC_XYZ/yxzyyxz_zx",
                "abABC_XYZ_ababbacca/abbac_xyyxzyyxz_zx",
                "abbac_xyyxzyyxz_zx/ABC_ba_XYZ_baccaba",
                "abbac_xyyxzyyxz_zx/caba_ABC_ba_XYZ_bac",
                "ababbacca/ABC_abbac_xyyxzyyxz_XYZ_zx",
                "ABC_ababbacca/abbac_xyyxzyyxz_XYZ_zx",
                "ABC_ba_XYZ_baccaba/abbac_xyyxzyyxz_zx",
                "ABC_baccaba_XYZ_ba/abbac_xyyxzyyxz_zx",
                "ABC_bbacbacacaba_XYZ_b/a_xyyxzyyxz_zx",
                "ababbacca/ABC_abbac_xyyxzyyxz_XYZzx",
                "ababbacca/ABC_abbac_xyyxzyyxzXYZzx",
                "ababbacca/ABCabbac_xyyxzyyxzXYZzx",
                "ababbaccaABCabbac_xyyxzyyxzXYZzx"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }  
    
    @Test
    public void test_synthetic_5() {
        pattern = "abcXYZ";
        
        variants = asList(
                "ABCXYZ_ababbacca/abbac_xyyxzyyxz_zx",
                "zx_ABCXYZ_acba/abac_xyyxz_zx"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }   
    
    @Test
    public void test_synthetic_6() {
        pattern = "abcXYZ";
        
        variants = asList(
                "ABC_baccaba_XYZ_ba/abbac_xyyxzyyxz_zx",
                "ABC_bbacbacacaba_XYZ_b/a_xyyxzyyxz_zx"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    } 
    
    @Test
    public void test_synthetic_7() {
        pattern = "abcXYZ";
        
        variants = asList(
                "abbac_xyyxzyyxz_zx/ABC_XYZ_bac_ba_caba",
                "abbac_xyyxzyyxz_zx/caba_bac_ba_ABC_XYZ"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_synthetic_8() {
        pattern = "abcXYZqwe";
        
        variants = asList(
                "aaa_ABC/dd_xyz_x/ff_qwe_xy",
                "abbac_qwe_xyyxzyyxz_zx/ABC_XYZ_bac_ba_caba"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_mock() {
        pattern = "abcdef";
        
        variants = asList(
                "acdebf"
        );
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }

    @Disabled("deduplication has been removed from implementation - it is now a duty of external code")
    @Test
    public void test_synthetic_7_duplicates() {
        pattern = "abcXYZ";
        
        variants = asList(
                "abbac_xyyxzyyxz_zx/ABC_XYZ_bac_ba_caba",
                "abbac_xyyxzyyxz_zx/caba_bac_ba_ABC_XYZ",
                "abbac_xyyxzyyxz_zx/ABC_XYZ_bac_ba_caba",
                "abbac_xyyxzyyxz_zx/caba_bac_ba_ABC_XYZ",
                "abbac_xyyxzyyxz_zx/ABC_XYZ_bac_ba_caba",
                "abbac_xyyxzyyxz_zx/caba_bac_ba_ABC_XYZ",
                "abbac_xyyxzyyxz_zx/caba_bac_ba_ABC_XYZ"
        );
        
        expected  = asList(
                "abbac_xyyxzyyxz_zx/ABC_XYZ_bac_ba_caba",
                "abbac_xyyxzyyxz_zx/caba_bac_ba_ABC_XYZ"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    private void weightVariantsAndCheckMatching() {
        boolean failed;
        try {
            totalVariantsQuantity = totalVariantsQuantity + variants.size();
            weightVariantsAndCheckMatchingInternally();
            failed = false;
        } catch (AssertionError e) {
            failed = true;
            if ( ! this.expectedToFail ) {
                throw e;
            }
        }        
        if ( ! failed && this.expectedToFail ) {
            fail("=== EXPECTED TO FAIL BUT PASSED ===");
        }
    }

    private void worseVariantsDontMatter() {
        notExpectedAreCritical = false;
    }
    
    private void weightVariantsAndCheckMatchingInternally() {
        if ( isNull(this.noWorseThan) ) {
            outputs = this.analyze.processStrings(pattern, variants);
        } else {
            outputs = this.analyze.processStrings(pattern, noWorseThan, variants);
        }        
        
        String expectedVariant;
        String actualVariant;
        String presentButNotExpectedLine;
        List<Output> nextSimilarVariants;
        
        List<String> reports = new ArrayList<>();
        List<String> presentButNotExpected = new ArrayList<>();        
        
        AtomicInteger counter = new AtomicInteger(0);
        int mismatches = 0;
        
        if ( expected.isEmpty() && outputs.size() > 0 ) {
            fail("No variants expected!");
        }
        
        while ( outputs.next() && ( counter.get() < expected.size() ) ) {
            
            if ( outputs.isCurrentMuchBetterThanNext() ) {
                
                expectedVariant = expected.get(counter.getAndIncrement());
                actualVariant = outputs.current().input();
                
                if ( actualVariant.equalsIgnoreCase(expectedVariant) ) {
                    reports.add(format("\n%s variant matches expected: %s", counter.get() - 1, expectedVariant));
                } else {
                    mismatches++;
                    reports.add(format(
                            "\n%s variant does not match expected: " +
                            "\n    expected : %s" +
                            "\n    actual   : %s", counter.get() - 1, expectedVariant, actualVariant));
                }
            } else {            
                nextSimilarVariants = outputs.nextSimilarSublist();
                for (Output weightedVariant : nextSimilarVariants) {
                    actualVariant = weightedVariant.input();
                    
                    if ( counter.get() < expected.size() ) {
                        expectedVariant = expected.get(counter.getAndIncrement());

                        if ( actualVariant.equalsIgnoreCase(expectedVariant) ) {
                            reports.add(format("\n%s variant matches expected: %s", counter.get() - 1, expectedVariant));
                        } else {
                            mismatches++;
                            reports.add(format(
                                "\n%s variant does not match expected: " +
                                "\n    expected : %s" +
                                "\n    actual   : %s", counter.get() - 1, expectedVariant, actualVariant));
                        }
                    } else {
                        presentButNotExpectedLine = format("\n %s", actualVariant);
                        if ( ! presentButNotExpected.contains(presentButNotExpectedLine) ) {
                            presentButNotExpected.add(presentButNotExpectedLine);
                        }
                    }    
                }
            }           
        } 
        
        if ( nonEmpty(reports) ) {
            reports.add("\n === Diff with expected === ");
        }
        
        if ( outputs.size() > expected.size() ) {
            int offset = expected.size();
            String presentButNotExpectedVariant;
            for (int i = offset; i < outputs.size(); i++) {
                presentButNotExpectedVariant = outputs.get(i).input();
                presentButNotExpectedLine = format("\n %s", presentButNotExpectedVariant);
                if ( ! presentButNotExpected.contains(presentButNotExpectedLine) ) {
                    presentButNotExpected.add(presentButNotExpectedLine);
                }
            }
        }
        
        boolean hasNotExpected = nonEmpty(presentButNotExpected);
        if ( hasNotExpected ) {
            presentButNotExpected.add(0, "\n === Present but not expected === ");
        }
        
        boolean hasMissed = counter.get() < expected.size();
        List<String> expectedButMissed = new ArrayList<>();
        if ( hasMissed ) {            
            expectedButMissed.add("\n === Expected but missed === ");
            
            while ( counter.get() < expected.size() ) {                
                expectedButMissed.add(format("\n%s variant missed: %s", counter.get(), expected.get(counter.getAndIncrement())));
            }
        }

        if ( hasNotExpected && !notExpectedAreCritical ) {
            log.info(presentButNotExpected.stream().collect(joining()));
        }
            
        if ( mismatches > 0 || hasMissed || (hasNotExpected && notExpectedAreCritical) ) {
            if ( hasMissed ) {
                reports.addAll(expectedButMissed);
            }
            if ( hasNotExpected ) {
                reports.addAll(presentButNotExpected);
            }
            reports.add(0, collectVariantsToReport());
            fail(reports.stream().collect(joining()));
        }
    }
    
    private String collectVariantsToReport() {
        List<String> variantsWithWeight = new ArrayList<>();
        outputs.resetTraversing();

        while ( outputs.next() ) {
            if ( outputs.isCurrentMuchBetterThanNext() ) {
                variantsWithWeight.add("\n" + outputs.current().input() + " is much better than next: " + outputs.current().weight());
            } else {
                variantsWithWeight.add("\nnext candidates are similar: ");                
                outputs
                        .nextSimilarSublist()
                        .forEach(output -> {
                            variantsWithWeight.add("\n  - " + output.input() + " : " + output.weight());
                        });
            }
        }
        if ( nonEmpty(variantsWithWeight) ) {            
            variantsWithWeight.add(0, "\n === Analyze result === ");
        }
        variantsWithWeight.add("");
        
        return variantsWithWeight.stream().collect(joining());
    }
    
}
