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
		
		if ( env.BRANCH_NAME == 'master' ) {
			checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'LocalBranch', localBranch: 'master'], [$class: 'RelativeTargetDirectory', relativeTargetDir: 'infra']], submoduleCfg: [], userRemoteConfigs: [[url: 'git@github.com:topicus-education-ops/k8s_onderwijs-intern_operations_applications.git', credentialsId: 'github_topicusonderwijs-buildbot_key']]])
				
			dir('infra/topical') {
				def reqs = readYaml file: 'requirements.yaml', text: "dependencies[0].version: '$buildTag'"
				writeYaml file: 'requirements.yaml', data: reqs
				git.commitAndPush("master", ['requirements.yaml'], "Deploy Topical at build " + buildNumber)
			}
		}
	}
	
//	notify {
//		emailNotificationRecipients = 'Luke.Niesink@topicus.nl'
//	}
}
