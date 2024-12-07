## how to deploy

- Bump the version
  - mvn version:set ${next_version}
  - and git add . && git ci -m "release ${next_version}"
- Deploy to staging repo
  - mvn clean deploy
- Release the staging repo
  - go to the staging repo: https://s01.oss.sonatype.org/#stagingRepositories
  - close the staging repo
  - if success, release and drop the staging repo
- Start to dev the next version
- Done
