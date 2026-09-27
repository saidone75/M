module M {
	requires java.xml;
	requires org.slf4j;
	requires ch.qos.reload4j;
    requires static lombok;
    exports org.saidone.m;
	exports org.saidone.utils;
	exports org.saidone.m.moves;
	exports org.saidone.m.moves.generators;
}