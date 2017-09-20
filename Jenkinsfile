config {
}

node() {
	git.checkout { }

	dockerfile.validate { }

	jekyll { }

	def img = dockerfile.build {
		name = 'topical/topical'
	}

	stage('publish') {
		img.push 'latest'
	}
}
