import plugin from 'tailwindcss/plugin'

/** @type {import('tailwindcss').Config} */
export default {
    content: ['./index.html', './example/src/**/*.{js,ts,jsx,tsx,scala}'],
    theme: {
        extend: {
            colors: {
                muted: 'hsl(var(--muted))',
                'muted-foreground': 'hsl(var(--muted-foreground))',
                background: 'hsl(var(--background))',
                glacier: 'var(--glacier)',
                'glacier-dark': 'var(--glacier-dark)',
            },
            boxShadow: {
                left: '-5px 0 5px -5px rgb(0 0 0 / 0.1)',
                right: '5px 0 5px -5px rgb(0 0 0 / 0.1)',
                bottom: '0 5px 5px -5px rgb(0 0 0 / 0.1)',
                top: '0 -5px 5px -5px rgb(0 0 0 / 0.1)',
                lr: '-5px 0 5px -5px rgb(0 0 0 / 0.1), 5px 0 5px -5px rgb(0 0 0 / 0.1)',
                lrb: '-5px 0 5px -5px rgb(0 0 0 / 0.1), 5px 0 5px -5px rgb(0 0 0 / 0.1), 0 5px 5px -5px rgb(0 0 0 / 0.1)',
            },
        },
    },
    plugins: [
        plugin(function ({addVariant}) {
            addVariant('icon', ['& svg'])
        }),
    ],
}
