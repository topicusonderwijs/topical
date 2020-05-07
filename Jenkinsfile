import nl.topicus.MavenCommands

config {
	concurrentBuilds = false
}

node() {
	catchError {
		git.checkout { }
	
		def mvn = new nl.topicus.MavenCommands()
		def buildNumber = env.BUILD_NUMBER
		def buildTag = mvn.getProjectVersion().replace("SNAPSHOT", buildNumber)
		def goal = env.BRANCH_NAME == 'master' ? "deploy" : "package"
		maven {
			goals = goal
			options = "-Ddocker.tag=${buildTag} -Dhelm.chartVersion=${buildTag}"
		}
	}
	
//	notify {
//		emailNotificationRecipients = 'Luke.Niesink@topicus.nl'
//	}
}
