// Variables we want to define with a script{} block should be declared outside the pipeline
def startdate = "UNKNOWN"

pipeline {
    // Default agent label to run all stages which don't override setting
    agent { label "linux" }

    parameters {
        string(defaultValue: '', description: 'Tag Version, e.g. v1.2.2', name: 'tagVersion')
        booleanParam(defaultValue: false, description: 'Make a release?', name: 'makeRelease')
    }

    options {
        // Tell Git about the build stages we're about to run
        // gitLabConnection('TODO')
        // gitlabBuilds(builds: ['Prep', 'Build'])
        
        // Add timestamps to console log
        timestamps()
    }
    // Default tools to install on agent
    tools {
        jdk 'jdk8'
        gradle 'gradle4'
    }
    
    stages {

        stage('Prep') {
            steps {
                gitlabCommitStatus(name: 'Prep') {
                	// Output parameters
                	echo "Create Release: ${env.makeRelease}"
                	echo "Tag Version: ${env.tagVersion}"

					// Some git state survives between builds, even tags that weren't pushed.  This gives us a clean slate to work from.
					script {    
	           			if ( params.makeRelease.equals(true) ) {                                               
							echo "Cleaning git cache for release."
							gitClean()
						}  
					}

                    // Check out revision that was used to fetch the Jenkinsfile running the pipeline
                    checkout scm
                    // Run groovy script, we can access the full Groovy sandbox and approved classes this way
                    script {
                        startdate = new Date().format("yyyyMMddHHmm")
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                // Run build on platforms in parallel
                parallel (
                    // Run on default agent
                    "linux": {
                   		//withCredentials([usernamePassword(credentialsId: 'TODO', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
	                        gitlabCommitStatus(name: 'Build') {
	                            sh "gradle clean -PmakeRelease=${env.makeRelease} -Partifactory_user=${USERNAME} -Partifactory_password=${PASSWORD}"
	                            sh "gradle -i -PmakeRelease=${env.makeRelease} -Partifactory_user=${USERNAME} -Partifactory_password=${PASSWORD}"
	                        }
                        //}
                    }
                )
            }
        }
        
        stage('Release') {
            steps {
	            script {
                	if (env.BRANCH_NAME.equals("master")) {
             			if ( params.makeRelease.equals(true) ) {                                               
		                    gitlabCommitStatus(name: 'Deliver') {
        						if (params.tagVersion.equals("") ){
	        						error("A valid tagVersion must be specified.  Invalid tagVersion: '${env.tagVersion}'")
								} else {
									echo "Tagging and pushing to Git"
	          						sh("git tag -a ${env.tagVersion} -m 'version ${env.tagVersion}'")
          							// TODO FIXME
          							//sshagent(['TODO']) {
	              					//	sh("git push --tags")
          							//}
								}
							}
						}						
					}
				}
			}
        }
        
        stage('Deploy') {
            steps {
	            script {
	                if (env.BRANCH_NAME.equals("master")) {
                    	gitlabCommitStatus(name: 'Deliver') {
	                    
                        	// TODO Fix credentials
                    		//withCredentials([usernamePassword(credentialsId: 'TODO', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                             	sh "gradle publish -Pdo_publishing=true -Partifactory_user=${USERNAME} -Partifactory_password=${PASSWORD} -PmakeRelease=${env.makeRelease}"						
                        	//}
                    	}
                	} else{
	                    gitlabCommitStatus(name: 'Deliver') {
                        	echo "Skipping publish on branch"
                    	}
                	}                
				}
			}
		}
    }
     // A post-pipeline "stage" to run various cleanup
    post {
    	failure {
            updateGitlabCommitStatus name: 'Build', state: 'failed'
        }
        success {
            updateGitlabCommitStatus name: 'Build', state: 'success'
        }    
        always {
            // Check if we skipped the real deliver stage to close out the build in GitLab
            script {
				
                // Generated these from the Pipeline Syntax page in Jenkins.
                jacoco classPattern: '**/build/classes', exclusionPattern: '**/edu/**', execPattern: '**/build/jacoco/*.exec'
                findbugs canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', pattern: '**/findbugs/main.xml', unHealthy: ''
                pmd canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '**/pmd/*.xml', unHealthy: ''
				junit '**/build/test-results/test/*.xml'
				deleteDir() /* clean up workspace */
            }
            
        }
    }
}

/**
 * Clean a Git project workspace.
 * Uses 'git clean' if there is a repository found.
 * Uses Pipeline 'deleteDir()' function if no .git directory is found.
 */
def gitClean() {
    timeout(time: 60, unit: 'SECONDS') {
        if (fileExists('.git')) {
            echo "Found Git repository: using Git to clean the tree."
            // The sequence of reset --hard and clean -fdx first
            // in the root and then using submodule foreach
            // is based on how the Jenkins Git SCM clean before checkout
            // feature works.
            sh 'git reset --hard'
            // Note: -e is necessary to exclude the temp directory
            // .jenkins-XXXXX in the workspace where Pipeline puts the
            // batch file for the 'bat' command.
            sh 'git clean -ffdx -e ".jenkins-*/"'
            sh 'git submodule foreach --recursive git reset --hard'
            sh 'git submodule foreach --recursive git clean -ffdx'
        }
        else
        {
            echo "No Git repository found: using deleteDir() to wipe clean"
            deleteDir()
        }
    }
}