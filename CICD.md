# Pipelines

We set up a Github Action for automatic build and exectution of the project.

## Steps

1. The action starts automatically on any kind of commit action on branch "dependability".
2. Ubuntu latest version
3. Checkout on Github machine for execution
4. Defining of JDK 21, for project parity
5. Setup of a maven command for lifecylce clean and verify for test execution
6. Upload of the Jacoco test coverage on Codecov