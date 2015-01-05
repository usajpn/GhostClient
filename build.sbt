name := "GhostClient"

version := "1.0"

val infinispanVersion = "6.0.2.Final"

resolvers += "Github" at "http://usajpn.github.io/GhostCommonLib-mvn-repo"

libraryDependencies ++= Seq(
  "GhostCommonLib" % "GhostCommonLib" % "0.28-BETA",
  "org.infinispan" % "infinispan-client-hotrod" % infinispanVersion excludeAll(
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling-river"),
    ExclusionRule(organization = "org.jboss.logging", name = "jboss-logging")
    ),
  "org.infinispan" % "infinispan-core" % infinispanVersion excludeAll(
    ExclusionRule(organization = "org.jgroups", name = "jgroups"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling-river"),
    ExclusionRule(organization = "org.jboss.marshalling", name = "jboss-marshalling"),
    ExclusionRule(organization = "org.jboss.logging", name = "jboss-logging"),
    ExclusionRule(organization = "org.jboss.spec.javax.transaction", name = "jboss-transaction-api_1.1_spec")
    ),
  "org.jgroups" % "jgroups" % "3.4.1.Final",
  "org.jboss.spec.javax.transaction" % "jboss-transaction-api_1.1_spec" % "1.0.1.Final",
  "org.jboss.marshalling" % "jboss-marshalling-river" % "1.4.4.Final",
  "org.jboss.marshalling" % "jboss-marshalling" % "1.4.4.Final",
  "org.jboss.logging" % "jboss-logging" % "3.1.2.GA",
  "net.jcip" % "jcip-annotations" % "1.0",
  "io.netty" % "netty-all" % "4.0.4.Final"
)
    