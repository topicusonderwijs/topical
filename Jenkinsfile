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
		
		stage("Deploy") {
			if ( env.BRANCH_NAME == 'master' ) {
				checkout([$class: 'GitSCM', branches: [[name: '*/staging']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'LocalBranch', localBranch: 'staging'], [$class: 'RelativeTargetDirectory', relativeTargetDir: 'infra']], submoduleCfg: [], userRemoteConfigs: [[url: 'git@github.com:topicusonderwijs/education-infra.git', credentialsId: 'github_topicusonderwijs-buildbot_key']]])
					
				dir('infra/topical') {
					def reqs = readYaml file: 'Chart.yaml'
					reqs.dependencies[0].version = "${buildTag}"
					sh "rm Chart.yaml"
					writeYaml file: 'Chart.yaml', data: reqs
					git.commitAndPush("staging", ['Chart.yaml'], "Deploy Topical ${buildTag} for build ${buildNumber}")
				}
			}
		}
	}
	
//	notify {
//		emailNotificationRecipients = 'Luke.Niesink@topicus.nl'
//	}
}
