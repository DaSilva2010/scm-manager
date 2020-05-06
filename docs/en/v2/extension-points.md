# Extension Points

The following extension points are provided for the frontend:

### admin.navigation
### admin.route
### admin.setting
### changeset.description
### changeset.right
### changesets.author.suffix
### group.navigation
### group.route
### group.setting
### main.route
- Add a new Route to the main Route (scm/)
- Props: authenticated?: boolean, links: Links

### plugins.plugin-avatar
### primary-navigation
### primary-navigation.first-menu
- A placeholder for the first navigation menu. 
- A PrimaryNavigationLink Component can be used here
- Actually this Extension Point is used from the Activity Plugin to display the activities at the first Main Navigation menu. 

### primary-navigation.logout
### profile.route
### profile.setting
### repo-config.route
### repos.branch-details.information
### repos.content.metadata
- Location: At meta data view for file
- can be used to render additional meta data line
- Props: file: string, repository: Repository, revision: string

### repos.create.namespace
### repos.sources.content.actionbar
### repository.navigation
### repository.navigation.topLevel
### repositoryRole.role-details.information
### repository.setting
### repos.repository-avatar
- Location: At each repository in repository overview
- can be used to add avatar for each repository (e.g., to mark repository type)

### repos.repository-details.information
- Location: At bottom of a single repository view
- can be used to show detailed information about the repository (how to clone, e.g.)

### repos.sources.view
### roles.route
### user.route
### user.setting


# Deprecated

### changeset.avatar-factory
- Location: At every changeset (detailed view as well as changeset overview)
- can be used to add avatar (such as gravatar) for each changeset
- expects a function: `(Changeset) => void`

### repos.sources.view
- Location: At sources viewer
- can  be used to render a special source that is not an image or a source code

### main.redirect
- Extension Point for a link factory that provide the Redirect Link 
- Actually used from the activity plugin: binder.bind("main.redirect", () => "/activity");

### markdown-renderer-factory
- A Factory function to create markdown [renderer](https://github.com/rexxars/react-markdown#node-types)
- The factory function will be called with a renderContext parameter of type Object. this parameter is given as a prop for the MarkdownView component.
 
**example:**


```javascript
let MarkdownFactory = (renderContext) => {
 
  let Heading= (props) => {
    return React.createElement(`h${props.level}`,
      props['data-sourcepos'] ? {'data-sourcepos': props['data-sourcepos']} : {},
      props.children);
  };
    return {heading : Heading};
};

binder.bind("markdown-renderer-factory", MarkdownFactory);
```

```javascript
<MarkdownView
    renderContext={{pullRequest, repository}}
    className="content"
    content={pullRequest.description}
/>
```
