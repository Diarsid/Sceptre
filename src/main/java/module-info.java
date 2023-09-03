module diarsid.sceptre {

    requires org.slf4j;
    requires diarsid.support;

    exports diarsid.sceptre.api;
    exports diarsid.sceptre.api.model;
    exports diarsid.sceptre.api.impl.logsinks;
}
