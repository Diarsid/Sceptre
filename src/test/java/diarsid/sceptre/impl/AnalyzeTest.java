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
import diarsid.sceptre.api.AnalyzeBuilder;
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
    private Outputs weightedOutputs;
    private boolean notExpectedAreCritical;
    
    public AnalyzeTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
        analyzeInstance = new AnalyzeBuilder()
                .withLogEnabled(true)
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
                "LostFilm");
        
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
                "Books/Tech/Java",
                "Tech",
                "Books/tech",
                "Books/Tech/Design", 
                "Tech/langs");
        
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
                "Films/Movies/Star.Wars.The.Last.Jedi.2017.D.BDRip.720p.ExKinoRay.mkv",
                "Content/WH/Game/Age_Of_Sigmar/Warscrolls"
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
        
        expectedSameOrderAsVariants();
        
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
                "mail",
                "gmail"
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
        
        expected = asList(
                "gmail"
        );
        
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
                "Books/Tech/Java",
                "Tech/langs");
        
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
                "Tools",
                "Books/Common/Tolkien_J.R.R");
        
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
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationService",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationGenerator");

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
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient"
                ,
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationGenerator"
        );

        expected = asList(
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationServiceClient",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationService",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationGenerator");

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
                "Books/Common/Tolkien_J.R.R", 
                "Books/Tech/Java");
        
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
    public void test_PriceAPICase_pricapi() {
        pattern = "pricapi";
        
        variants = asList(            
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/PriceCalculationAPI");
        
        expected = asList(
                "Projects/UkrPoshta/PriceCalculationAPI",
                "Projects/UkrPoshta/CainiaoAPI");
        
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
                "D:/DEV/3__Tools/Servers/Virtualization_Servers",
                "There's a Spiritual Solution to Every Problem by Dr. Wayne W. Dyer"
                );

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
                "How the Irish Saved Civilization: The Untold Story of Ireland's Heroic Role from the Fall of Rome to the Rise of Medieval Europe by Thomas Cahill",
                "The Golden Key by Jennifer Roberson and Melanie Rawn, and Kate Elliott");

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
                "The Wars of Gods and Men: Book III of the Earth Chronicles (The Earth Chronicles) by Zecharia Sitchin",
                "On Killing: The Psychological Cost of Learning to Kill in War and Society by Dave Grossman");

        expectedSameOrderAsVariants();

        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_godmnwar() {
        pattern = "godmnwar";

        variants = asList(
                "The Wars of Gods and Men: Book III of the Earth Chronicles (The Earth Chronicles) by Zecharia Sitchin",
                "On Killing: The Psychological Cost of Learning to Kill in War and Society by Dave Grossman");

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
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaStatusNotificationService",
                "D:/DEV/1__Projects/UkrPoshta/UkrposhtaCommons");

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
            weightedOutputs = this.analyze.processStrings(pattern, variants);
        } else {
            weightedOutputs = this.analyze.processStrings(pattern, noWorseThan, variants);
        }        
        
        String expectedVariant;
        String actualVariant;
        String presentButNotExpectedLine;
        List<Output> nextSimilarVariants;
        
        List<String> reports = new ArrayList<>();
        List<String> presentButNotExpected = new ArrayList<>();        
        
        AtomicInteger counter = new AtomicInteger(0);
        int mismatches = 0;
        
        if ( expected.isEmpty() && weightedOutputs.size() > 0 ) {
            fail("No variants expected!");
        }
        
        while ( weightedOutputs.next() && ( counter.get() < expected.size() ) ) {
            
            if ( weightedOutputs.isCurrentMuchBetterThanNext() ) {
                
                expectedVariant = expected.get(counter.getAndIncrement());
                actualVariant = weightedOutputs.current().input();
                
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
                nextSimilarVariants = weightedOutputs.nextSimilarSublist();
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
        
        if ( weightedOutputs.size() > expected.size() ) {
            int offset = expected.size();
            String presentButNotExpectedVariant;
            for (int i = offset; i < weightedOutputs.size(); i++) {
                presentButNotExpectedVariant = weightedOutputs.get(i).input();
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
        weightedOutputs.resetTraversing();

        while ( weightedOutputs.next() ) {
            if ( weightedOutputs.isCurrentMuchBetterThanNext() ) {
                variantsWithWeight.add("\n" + weightedOutputs.current().input() + " is much better than next: " + weightedOutputs.current().weight());
            } else {
                variantsWithWeight.add("\nnext candidates are similar: ");                
                weightedOutputs
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
