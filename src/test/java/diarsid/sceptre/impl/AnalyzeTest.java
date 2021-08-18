package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.sceptre.api.model.Variant;
import diarsid.sceptre.api.model.Variants;
import diarsid.strings.similarity.api.Similarity;
import diarsid.support.objects.GuardedPool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import static diarsid.support.configuration.Configuration.actualConfiguration;
import static diarsid.support.configuration.Configuration.configure;
import static diarsid.support.objects.Pools.pools;
import static diarsid.support.objects.collections.CollectionUtils.nonEmpty;
import static org.junit.jupiter.api.Assertions.fail;

public class AnalyzeTest {
    
    private static WeightAnalyzeReal analyzeInstance;
    private static int totalVariantsQuantity;
    private static long start;
    private static long stop;

    static {
        configure().withDefault(
                "log = true",
                "analyze.weight.base.log = true",
                "analyze.weight.positions.search.log = true",
                "analyze.weight.positions.clusters.log = true",
                "analyze.result.variants.limit = 11",
                "analyze.similarity.log.base = true",
                "analyze.similarity.log.advanced = true");
    }
    
    private WeightAnalyzeReal analyze;
    private boolean expectedToFail;
    private String pattern;
    private String noWorseThan;
    private List<String> variants;
    private List<String> expected;
    private Variants weightedVariants;
    
    public AnalyzeTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
        Similarity similarity = Similarity.createInstance(actualConfiguration());
        analyzeInstance = new WeightAnalyzeReal(actualConfiguration(), similarity, pools());
        start = currentTimeMillis();
    }
    
    @AfterAll
    public static void tearDownClass() {
        stop = currentTimeMillis();
        Logger logger = LoggerFactory.getLogger(AnalyzeTest.class);
        String report = 
                "\n ======================================" +
                "\n ====== Total AnalyzeTest results =====" +
                "\n ======================================" +
                "\n  total time     : %s " + 
                "\n  total variants : %s \n";
        logger.info(format(report, stop - start, totalVariantsQuantity));
        Optional<GuardedPool<AnalyzeUnit>> pool = pools().poolOf(AnalyzeUnit.class);
        if ( pool.isPresent() ) {
            GuardedPool<AnalyzeUnit> c = pool.get();
            AnalyzeUnit analyzeData = c.give();
        }
    }
    
    @BeforeEach
    public void setUp() {
        this.analyze = analyzeInstance;
    }
    
    @AfterEach
    public void tearDown() {
        this.analyze.resultsLimitToDefault();
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
                "Images/Photos");
        
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
        
        expectedSameOrderAsVariants();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_FilmsListCase_lsftilm() {
        pattern = "lsftilm";
        
        variants = asList(
                "LostFilm",
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
                "Books/Tech/Java",
                "Tech",
                "Books/tech",
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
    public void test_messageServers_msser() {
        pattern = "msser";
        
        variants = asList(
                "Tools/Servers/Messaging_Servers",
                "Images/Photos/Miniatures"
                );
        
        expectedSameOrderAsVariants();
        
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
                "Content/WH/Game/The_9th_Age",
                "Image"
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
                "1__Projects/Diarsid/NetBeans",
                "2__LIB/Maven_Local_Repo/io/springfox/springfox-bean-validators"
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
                "netbeans_projects",
                "beam_project/src",
                "beam netpro",
                "babel_pro",
                "abe_netpro");
        
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
                "D:/DEV/1__Projects/X__Archive/Diarsid",
                "D:/DEV/1__Projects/Diarsid/X__GitHub_Pages",
                "D:/DEV/1__Projects/Diarsid/X__Distrib"
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
                "beam_server_project",
                "netbeans_projects"
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
    public void test_jobCurrentCase_jbo_cruent() {
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
                "Books/Tech/Java",
                "Current_Job/Workspace");
        
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
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI");
        
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
                "Projects/Diarsid/WebStorm/React.js",
                "Java/Specifications/JPA_v.2.2_(JSR_318).pdf"
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
    
    @Test
    public void test_synthetic_4() {
        this.analyze.disableResultsLimit();
        
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
                "axABCXYZacba",
                "axABCXYZ_abaca/ab_xyyxz_zx",
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
    
    private void weightVariantsAndCheckMatchingInternally() {
        if ( isNull(this.noWorseThan) ) {
            weightedVariants = this.analyze.weightStrings(pattern, variants);
        } else {
            weightedVariants = this.analyze.weightStrings(pattern, noWorseThan, variants);
        }        
        
        String expectedVariant;
        String actualVariant;
        List<Variant> nextSimilarVariants;
        
        List<String> reports = new ArrayList();        
        List<String> presentButNotExpected = new ArrayList<>();        
        
        AtomicInteger counter = new AtomicInteger(0);
        int mismatches = 0;
        
        if ( expected.isEmpty() && weightedVariants.size() > 0 ) {
            fail("No variants expected!");
        }
        
        while ( weightedVariants.next() && ( counter.get() < expected.size() ) ) {
            
            if ( weightedVariants.currentIsMuchBetterThanNext() ) {
                
                expectedVariant = expected.get(counter.getAndIncrement());
                actualVariant = weightedVariants.current().value();
                
                if ( actualVariant.equalsIgnoreCase(expectedVariant) ) {
                    reports.add(format("\n%s variant matches expected: %s", counter.get() - 1, expectedVariant));
                } else {
                    mismatches++;
                    reports.add(format(
                            "\n%s variant does not match expected: \n" +
                            "    expected : %s\n" +
                            "    actual   : %s", counter.get() - 1, expectedVariant, actualVariant));
                }
            } else {            
                nextSimilarVariants = weightedVariants.nextSimilarVariants();
                for (Variant weightedVariant : nextSimilarVariants) {
                    actualVariant = weightedVariant.value();
                    
                    if ( counter.get() < expected.size() ) {
                        expectedVariant = expected.get(counter.getAndIncrement());

                        if ( actualVariant.equalsIgnoreCase(expectedVariant) ) {
                            reports.add(format("\n%s variant matches expected: %s", counter.get() - 1, expectedVariant));
                        } else {
                            mismatches++;
                            reports.add(format(
                                "\n%s variant does not match expected: \n" +
                                "    expected : %s\n" +
                                "    actual   : %s", counter.get() - 1, expectedVariant, actualVariant));
                        }
                    } else {
                        presentButNotExpected.add(format("\n %s\n", actualVariant));
                    }    
                }
            }           
        } 
        
        if ( nonEmpty(reports) ) {
            reports.add("\n === Diff with expected === ");
        }
        
        if ( weightedVariants.size() > expected.size() ) {
            int offset = expected.size();
            String presentButNotExpectedVariant;
            for (int i = offset; i < weightedVariants.size(); i++) {
                presentButNotExpectedVariant = weightedVariants.getVariantAt(i);
                presentButNotExpected.add(format("\n %s\n", presentButNotExpectedVariant));
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
            
        if ( mismatches > 0 || hasMissed || hasNotExpected ) {    
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
        weightedVariants.resetTraversing();

        while ( weightedVariants.next() ) {            
            if ( weightedVariants.currentIsMuchBetterThanNext() ) {
                variantsWithWeight.add("\n" + weightedVariants.current().value() + " is much better than next: " + weightedVariants.current().weight());
            } else {
                variantsWithWeight.add("\nnext candidates are similar: ");                
                weightedVariants.nextSimilarVariants()
                        .stream()
                        .forEach(candidate -> {
                            variantsWithWeight.add("\n  - " + candidate.value() + " : " + candidate.weight());
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
