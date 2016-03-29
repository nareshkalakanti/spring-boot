import groovy.util.XmlSlurper

new File(project.build.directory, 'generated-resources/starters.adoc').withPrintWriter { writer ->
	writer.println '|==='
	writer.println '| Starter | Description'
	new File(project.build.directory, 'external-resources/starter-poms').eachDir { starter ->
		def root = new XmlSlurper().parse(new File(starter, 'pom.xml'))
		writer.println ''
		writer.println "| ${root.name.text()}"
		writer.println "| ${root.description.text()}"
	}
	writer.println '|==='
}
