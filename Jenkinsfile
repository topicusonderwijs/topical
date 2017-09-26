config {
}

node() {
	git.checkout { }

	dockerfile.validate { }

	def img = dockerfile.build {
		name = 'topical/topical'
	}

	stage('publish') {
		img.push 'latest'
	}
}
