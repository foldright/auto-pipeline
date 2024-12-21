import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Easy to Use',
    Svg: require('@site/static/img/undraw_docusaurus_mountain.svg').default,
    description: (
      <>
        Transform your components with minimal effort - simply add the <code>@AutoPipeline</code> annotation 
        and let the magic happen during compilation. Experience the power of pipeline architecture without the complexity!
      </>
    ),
  },
  {
    title: 'Make Your Component More Extensible',
    Svg: require('@site/static/img/undraw_docusaurus_tree.svg').default,
    description: (
      <>
        Unlock unlimited potential with the pipeline pattern. The generated code follows best practices 
        for scalability and modularity, making it effortless to extend and maintain your components. 
        Dive into our examples to discover the possibilities!
      </>
    ),
  },
  {
    title: 'Zero Dependency',
    Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
        Keep your runtime clean and efficient. Auto Pipeline only works its magic during compilation, 
        and the generated code relies solely on the Java SDK. No external dependencies, no runtime overhead 
        - just pure, efficient code.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        {/*<Svg className={styles.featureSvg} role="img" />*/}
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
