package src

ModsDotGroovy.make {
    modLoader = 'lowcode'
    loaderVersion = '[1,)'

    license = 'LGPL-3.0-or-later'
    issueTrackerUrl = 'https://github.com/lukebemish/ModularMetals/issues'

    mod {
        modId = "modularmetals_test"
        displayName = "Modular Metals - Test"
        version = this.version
        group = this.group
        intermediate_mappings = 'net.fabricmc:intermediary'
        displayUrl = 'https://github.com/lukebemish/ModularMetals'

        description = "Tests that ModularMetals works"
        authors = [this.buildProperties['mod_author'] as String]

        dependencies {
            minecraft = this.minecraftVersionRange
            mod('modularmetals') {
                versionRange = ">=${this.modularMetalsVersion}"
            }
        }
    }
}
