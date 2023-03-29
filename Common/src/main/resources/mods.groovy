ModsDotGroovy.make {
    modLoader = 'gml'
    loaderVersion = '[1,)'

    license = 'LGPL-3.0-or-later'
    issueTrackerUrl = 'https://github.com/lukebemish/ModularMetals/issues'

    mod {
        modId = this.buildProperties['mod_id']
        displayName = this.buildProperties['mod_name']
        version = this.version
        group = this.group
        intermediate_mappings = 'net.fabricmc:intermediary'
        displayUrl = 'https://github.com/lukebemish/ModularMetals'

        description = "Adds modularly defined metals"
        authors = [this.buildProperties['mod_author'] as String]

        dependencies {
            minecraft = this.minecraftVersionRange

            forge {
                versionRange = ">=${this.forgeVersion}"
            }
            onForge {
                mod('gml') {
                    versionRange = ">=${this.libs.versions.gml}"
                }
            }

            onQuilt {
                mod('groovyduvet') {
                    versionRange = ">=${this.libs.versions.groovyduvet}"
                }
            }

            quiltLoader {
                versionRange = ">=${this.quiltLoaderVersion}"
            }

            mod('dynamic_asseet_generator') {
                versionRange = ">=${this.libs.versions.dynassetgen}"
            }

            mod('defaultresources') {
                versionRange = ">=${this.libs.versions.defaultresources}"
            }
        }

        entrypoints {
            init = [
                    adapted {
                        adapter = 'groovyduvet'
                        value = 'dev.lukebemish.modularmetals.quilt.ModularMetalsQuilt'
                    }
            ]
        }
    }
    onQuilt {
        mixin = [
            'mixin.modularmetals.quilt.json',
            'mixin.modularmetals.json'
        ]
        access_widener = 'modularmetals.accesswidener'
    }
}
