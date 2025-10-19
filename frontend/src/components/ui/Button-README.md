# Button Component

## Overview

The `Button` component is a versatile, customizable button that supports
multiple variants, sizes, and icon configurations.

## Props

| Prop Name   | Type    | Default  | Possible Values                  | Description                       |
| ----------- | ------- | -------- | -------------------------------- | --------------------------------- |
| `text`      | String  | 'Button' | Any string                       | Button text                       |
| `size`      | String  | 'medium' | 'medium', 'large'                | Button size                       |
| `variant`   | String  | 'filled' | 'filled', 'outlined', 'gradient' | Button style variant              |
| `leftIcon`  | String  | null     | Material Icons name              | Icon to display on the left side  |
| `rightIcon` | String  | null     | Material Icons name              | Icon to display on the right side |
| `disabled`  | Boolean | false    | true, false                      | Disable the button                |

## Usage Examples

### Basic Button

```vue
<Button text="Click Me" />
```

### Variants

```vue
<!-- Filled Button (Default) -->
<Button text="Filled Button" variant="filled" />

<!-- Outlined Button -->
<Button text="Outlined Button" variant="outlined" />

<!-- Gradient Button -->
<Button text="Gradient Button" variant="gradient" />
```

### Sizes

```vue
<!-- Medium Button (Default) -->
<Button text="Medium Button" size="medium" />

<!-- Large Button -->
<Button text="Large Button" size="large" />
```

### Icons

```vue
<!-- Left Icon -->
<Button text="Add Item" leftIcon="add" />

<!-- Right Icon -->
<Button text="Next" rightIcon="arrow_forward" />

<!-- Both Icons -->
<Button text="Favorite" leftIcon="star" rightIcon="star" />
```

### Disabled State

```vue
<Button text="Disabled Button" :disabled="true" />
```

### Event Handling

```vue
<Button text="Click Me" @click="handleClick" />
```

## Notes

- Requires Material Icons for icon display
- Uses 'Inter' font family
- Responsive design with consistent styling
- Smooth hover and active state transitions
