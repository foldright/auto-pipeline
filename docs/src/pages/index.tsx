import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import Heading from '@theme/Heading';

import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
            <Heading as="h1" className="hero__title">
                {siteConfig.title}
            </Heading>
            {/*<img src="img/logo.svg" alt="autopipeline Logo" className={styles.logo} style={{maxWidth: '20vh'}}/>*/}
            <img src="https://github.com/user-attachments/assets/358ed4a2-cfe2-4a63-87d5-8c7c9bf3362f" alt="autopipeline Logo" className={styles.logo} style={{maxWidth: '20vh'}}/>
            <p className="hero__subtitle">
                {siteConfig.tagline} <br/>
                {siteConfig.customFields.tagline2 as string}
            </p>
            <div className={styles.buttons}>
                <Link
                    className="button button--secondary button--lg"
                    to="/docs/intro">
                    Quick Start - 5min ⏱️
                </Link>
            </div>
        </div>
    </header>
  );
}

export default function Home(): JSX.Element {
    const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description="Description will go into a meta tag in <head />">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
