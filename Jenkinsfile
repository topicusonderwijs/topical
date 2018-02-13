config { }

node() {
	catchError {
		git.checkout { }
	
		maven { }
		
		if (env.BRANCH_NAME == 'master') {
			stage('Deploy') {
				def workspace = pwd()
				def dockerComposeFile = readFile "${workspace}/docker-compose.json"
				def rancherComposeFile = readFile "${workspace}/rancher-compose.json"
				
				rancher {
					credentialsID = "rancher-operations-api-key"
					dockerCompose = dockerComposeFile
					rancherCompose = rancherComposeFile
					environmentId = "1a57"
					stackGroup = "topicus"
					stackName = "topical"
					stackDescription = "Vergaderkameroverzicht Singel 9"
					externalId = "topical-latest"
				}
			}
		}
	}
	
//	notify {
//		emailNotificationRecipients = 'Luke.Niesink@topicus.nl'
//	}
}
