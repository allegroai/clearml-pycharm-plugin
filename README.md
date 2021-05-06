# ClearML - PyCharm Plugin


[![GitHub license](https://img.shields.io/github/license/allegroai/clearml-pycharm-plugin.svg)](https://img.shields.io/github/license/allegroai/clearml-pycharm-plugin.svg)
[![GitHub release](https://img.shields.io/github/release-pre/allegroai/clearml-pycharm-plugin.svg)](https://img.shields.io/github/release-pre/allegroai/clearml-pycharm-plugin.svg)

The **ClearML PyCharm plugin** enables syncing local execution configuration to a remote executor machine:

* Sync local repository information to a remote debug machine.

* Multiple users can use the same resource for execution without compromising private credentials.

* Run the [ClearML Agent](https://github.com/allegroai/clearml-agent) on default VMs/Containers.

## Installation

1. Download the latest plugin version from the [Releases page](https://github.com/allegroai/clearml-pycharm-plugin/releases)

1. Install the plugin in PyCharm from local disk:

![Alt Text](https://github.com/allegroai/clearml-pycharm-plugin/blob/master/docs/pycharm_plugin_from_disk.png?raw=true)

## Optional: ClearML configuration parameters

> **Warning**: If you set ClearML configuration parameters (ClearML Server and ClearML credentials) in the plugin, they will override those settings in the ClearML configuration file.

1. In PyCharm, open Settings -> Tools -> ClearML

1. Configure your ClearML servers information:

    1. API server (for example: http://localhost:8008)
    1. Web server (for example: http://localhost:8080)
    1. File server  (for example: http://localhost:8081)

1. Add your ClearML user credentials key/secret

![Alt Text](https://github.com/allegroai/clearml-pycharm-plugin/blob/master/docs/pycharm_config_params.png?raw=true)

## Additional Documentation

For detailed information about the **ClearML** open source suite, see our [ClearML Documentation](https://allegro.ai/clearml/docs).

## Community & Support

* If you have a question, consult our **ClearML** [FAQs](https://allegro.ai/docs/faq/faq) or tag your questions on [stackoverflow](https://stackoverflow.com/questions/tagged/clearml) with "*clearml*".
* To request features or report bugs, see our [GitHub issues](https://github.com/allegroai/clearml-pycharm-plugin/issues).
* Email us at *[support@clear.ml](mailto:support@clear.ml?subject=ClearML PyCharm Plugin)*

## Contributing

We encourage your contributions! See our **ClearML** [Guidelines for Contributing](https://github.com/allegroai/clearml/blob/master/docs/contributing.md).

